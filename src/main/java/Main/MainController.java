package Main;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.*;

public class MainController {
    @FXML
    public ImageView mainView;
    @FXML
    public ListView<String> mainList, waypoints,routeDetails;
    @FXML
    public ComboBox<String> source,destination;
    @FXML
    public RadioButton quickest,scenic,multiple;
    @FXML
    public Slider routeLimit;
    Image map, blackAndWhite;
    public Label details;
    public Map<Integer,CoolNode<Pixel>> pixels;
    public Map<Integer,CoolNode<Room>> rooms;
    public List<List<CoolNode<Room>>> paths;



    @FXML
    void initialize() throws FileNotFoundException {
        map = new Image(new FileInputStream("src/main/resources/main/map.png"));
        blackAndWhite = new Image(new FileInputStream("src/main/resources/main/blackandwhite.png"));
        mainView.setImage(map);
        pixels = new HashMap<>();
        rooms = new HashMap<>();
        try{
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        ObjectInputStream is = xstream.createObjectInputStream(new FileReader("RoomInfo.xml"));
        rooms = (Map<Integer,CoolNode<Room>>) is.readObject();
        is.close();}
        catch (IOException | ClassNotFoundException ioException){
            System.err.println(ioException.getMessage());
        }


        for(Map.Entry<Integer,CoolNode<Room>> room:rooms.entrySet()){
            source.getItems().add(room.getValue().getContents().getName());
        }

        int [] blackAndWhiteArray = new int[(int) (blackAndWhite.getWidth()*blackAndWhite.getHeight())];
        int width = (int) blackAndWhite.getWidth(), height = (int) blackAndWhite.getHeight();
        for(int x =0;x<width;++x){
            for(int y = 0;y<height;++y){
                if(blackAndWhite.getPixelReader().getColor(x,y).equals(Color.WHITE)) {
                    blackAndWhiteArray[y * width + x] = 1;
                }
                else
                    blackAndWhiteArray[y*width+x] = 0;
            }
        }



        for(int i = width; i<blackAndWhiteArray.length-width;++i){
            if(blackAndWhiteArray[i]==1&&i%width!=0&&i%width!=width-1){
                CoolNode<Pixel> pixel = new CoolNode<>(new Pixel(i%width,i/width));
                if(blackAndWhiteArray[i-width]==1) pixel.connectToNodeUndirected(pixels.get(i-width));
                if(blackAndWhiteArray[i-1]==1) pixel.connectToNodeUndirected(pixels.get(i-1));
                pixels.put(i,pixel);
            }
        }


        waypoints.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

//        Adds listener to the source to update rooms
        source.valueProperty().addListener(e -> {
            destination.getItems().clear();
            waypoints.getItems().clear();
            Room sourceRoom = getRoom(source.getValue()).getContents();
            for(Map.Entry<Integer,CoolNode<Room>> room:rooms.entrySet()) {
                if (!sourceRoom.equals(room)) {
                    destination.getItems().add(room.getValue().getContents().getName());
                    waypoints.getItems().add(room.getValue().getContents().getName());
                }
            }
        });
//        adds listener to the waypoints to keep them updated with rooms
        waypoints.setOnMouseClicked(e -> {
            Room sourceRoom = getRoom(source.getValue()).getContents();
            destination.getItems().clear();
            for(Map.Entry<Integer,CoolNode<Room>> room:rooms.entrySet()){
                if(!waypoints.getSelectionModel().getSelectedItems().contains(room.getValue().getContents().getName())&&!sourceRoom.equals(room))
                destination.getItems().add(room.getValue().getContents().getName());
            }
        });
        mainList.setOnMouseClicked(e -> drawMultiRoute());
    }

    void drawMultiRoute(){
        mainView.setImage(map);
        List<CoolNode<Room>> route = paths.get(mainList.getSelectionModel().getSelectedIndex());
        routeDetails.getItems().clear();
        routeDetails.getItems().add(0+": "+route.get(0).getContents().getName());
        for(int i = 1;i<route.size();++i){
            Room current = route.get(i).getContents(), prev = route.get(i-1).getContents();
            mainView.setImage(Utilities.drawLine(mainView.getImage(),current.getPixelX(),current.getPixelY(),prev.getPixelX(),prev.getPixelY(),Color.BLUE));
            mainView.setImage(Utilities.drawLine(mainView.getImage(),prev.getPixelX(),prev.getPixelY(),current.getPixelX(),current.getPixelY(),Color.BLUE));
            routeDetails.getItems().add(i+": "+current.getName());
        }

    }
    @FXML
    void calculateRoute(){
        mainView.setImage(map);
        List<CoolNode<Room>> temp = new ArrayList<>();
        temp.add(getRoom(source.getValue()));
        paths = findAllPathsDepthFirst(getRoom(source.getValue()),temp,getRoom(destination.getValue()));
        int limit = (int) routeLimit.getValue();
        paths.removeIf(e ->
             !e.contains(waypoints.getItems())
        );
        if(paths.size()>limit)
        paths = paths.subList(0,limit);
        mainList.getItems().clear();
        for(int i=0;i<paths.size();++i){
            mainList.getItems().add("Route: "+(i+1));
        }
//        mainList.getSelectionModel().select(0);
//        drawMultiRoute();

    }
    public int routeLength(List<CoolNode<Room>> route){
        int distance = 0;
        for(int i = 1;i<route.size();++i){
            Room current = route.get(i).getContents(), prev = route.get(i-1).getContents();
            distance+=Utilities.distance(prev.getPixelX(),prev.getPixelY(),current.getPixelX(),current.getPixelY());
        }
        return distance;
    }

    public static  List<List<CoolNode<Room>>> findAllPathsDepthFirst(CoolNode<Room> from, List<CoolNode<Room>> encountered,CoolNode<Room> lookingfor){
        List<List<CoolNode<Room>>> result=null, temp2;
        if(from.equals(lookingfor)) { //Found it
            List<CoolNode<Room>> temp=new ArrayList<>(); //Create new single solution path list
            temp.add(from); //Add current node to the new single path list
            result=new ArrayList<>(); //Create new "list of lists" to store path permutations
            result.add(temp); //Add the new single path list to the path permutations list
            return result; //Return the path permutations list
        }
        if(encountered==null) encountered=new ArrayList<>(); //First node so create new (empty) encountered list
        encountered.add(from); //Add current node to encountered list
        for(CoolNode<Room> adjNode : from.getAttachedNodes()){
            if(!encountered.contains(adjNode)) {
                temp2=findAllPathsDepthFirst(adjNode,new ArrayList<>(encountered),lookingfor); //Use clone of encountered list
//for recursive call!
                if(temp2!=null) { //Result of the recursive call contains one or more paths to the solution node
                    for(List<CoolNode<Room>> x : temp2) //For each partial path list returned
                        x.add(0,from); //Add the current node to the front of each path list
                    if(result==null) result=temp2; //If this is the first set of solution paths found use it as the result
                    else result.addAll(temp2); //Otherwise append them to the previously found paths
                }
            }
        }
        return result;
    }

//    public static <T> List<CoolNode<Room>> findPathBreadthFirst(CoolNode<Room> startNode, T lookingfor){
//        List<List<CoolNode<Room>>> agenda=new ArrayList<>(); //Agenda comprised of path lists here!
//        List<CoolNode<Room>> firstAgendaPath=new ArrayList<>(),resultPath;
//        firstAgendaPath.add(startNode);
//        agenda.add(firstAgendaPath);
//        resultPath=findPathBreadthFirst(agenda,null,lookingfor); //Get single BFS path (will be shortest)
//        Collections.reverse(resultPath); //Reverse path (currently has the goal node as the first item)
//        return resultPath;
//    }
//
//    public CoolNode<Room> getRoom(int ID){
//        for(CoolNode<Room> room:rooms){
//            if(room.getContents().getID() == ID)
//                return room;
//        }
//        return null;
//    }
    public CoolNode<Room> getRoom(String name){
        for(Map.Entry<Integer,CoolNode<Room>> room:rooms.entrySet()){
            if(room.getValue().getContents().getName().equals(name))
                return room.getValue();
        }
        return null;
    }

//    public CoolNode<Pixel> getPixel(int ID){
//        for(CoolNode<Pixel> pixel :pixels){
//            if(pixel.getContents().getID()==ID)
//                return pixel;
//        }
//        return null;
//    }
    public void save(int[] savedItem, String fileName) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        ObjectOutputStream out = xstream.createObjectOutputStream(new FileWriter(fileName));
        out.writeObject(savedItem);
        out.close();
    }

}
