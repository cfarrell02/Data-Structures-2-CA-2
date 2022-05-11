package Main;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.*;

public class MainController {
    @FXML
    public ImageView mainView;
    @FXML
    public ListView<String> mainList, waypoints,exclusions,routeDetails;
    @FXML
    public ComboBox<String> source,destination;
    @FXML
    public ChoiceBox<String> algorithmType;
    @FXML
    public RadioButton quickest,scenic,multiple;
    @FXML
    public Slider routeLimit;
    @FXML
    Image map, blackAndWhite;
    @FXML
    TextField sourceTextField, destinationTextField;
    @FXML
    Button sourceButton, destinationButton;
    public Map<Integer,CoolNode<Pixel>> pixels;
    int [] blackAndWhiteArray;
    public Map<Integer,CoolNode<Room>> rooms;
    public List<List<CoolNode<Room>>> paths;
    @FXML
    public AnchorPane leftPane, rightPane;
    //public List<CoolNode<Room>> route;

    TextField currentTextField = null;



    @FXML
    void initialize() throws FileNotFoundException {
        map = new Image(new FileInputStream("src/main/resources/main/map.png"));
        blackAndWhite = new Image(new FileInputStream("src/main/resources/main/blackandwhite.png"));
        blackAndWhiteArray = new int[(int) (blackAndWhite.getWidth()*blackAndWhite.getHeight())];
        mainView.setImage(map);
        pixels = new HashMap<>();
        rooms = new HashMap<>();
        algorithmType.getItems().add("Dijkstra's Algorithm");
        algorithmType.getItems().add("Breadth First Search Algorithm");
        algorithmType.setValue("Dijkstra's Algorithm");
        try{
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        ObjectInputStream is = xstream.createObjectInputStream(new FileReader("RoomInfo.xml"));
        rooms = (Map<Integer,CoolNode<Room>>) is.readObject();
        is.close();}
        catch (IOException | ClassNotFoundException ioException){
            System.err.println("XML Load Error: \n"+ioException.getMessage());

        }



        for(Map.Entry<Integer,CoolNode<Room>> room:rooms.entrySet()){
            source.getItems().add(room.getValue().getContents().getName());
        }


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
                CoolNode<Pixel> pixel = new CoolNode<>(new Pixel(i,i%width,i/width));
                if(blackAndWhiteArray[i-width]==1) pixel.connectToNodeUndirected(pixels.get(i-width));
                if(blackAndWhiteArray[i-1]==1) pixel.connectToNodeUndirected(pixels.get(i-1));
//                if(blackAndWhiteArray[i-width-1]==1) pixel.connectToNodeUndirected(pixels.get(i-width-1));
//                if(blackAndWhiteArray[i-width+1]==1) pixel.connectToNodeUndirected(pixels.get(i-width+1));
                pixels.put(i,pixel);
            }
        }


       //waypoints.getSelectionModel().setSelectionMod e(SelectionMode.MULTIPLE);

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
        mainList.setOnMouseClicked(e ->
        {
            List<CoolNode<Room>> route = paths.get(mainList.getSelectionModel().getSelectedIndex());
            if(route!=null)
            drawRoute(route);
        });
        quickest.selectedProperty().addListener(e->{
            algorithmType.setDisable(!quickest.isSelected());
        });

//        mainView.setOnMouseMoved(e -> {
//            WritableImage wr = new WritableImage(map.getPixelReader(),(int) map.getWidth(),(int) map.getHeight());
//            CoolNode<Pixel> pixel = pixels.get((int) (e.getY()*width+e.getX()));
//            if(pixel==null) return;
//            wr.getPixelWriter().setColor(pixel.getContents().getX(),pixel.getContents().getY(),Color.BLUE);
//            for(CoolNode<Pixel> pixelCoolNode: pixel.getAttachedNodes()){
//                wr.getPixelWriter().setColor(pixelCoolNode.getContents().getX(),pixelCoolNode.getContents().getY(),Color.PINK);
//            }
//            mainView.setImage(wr);
//        });

       source.valueProperty().addListener(((ov, oldValue, newValue) -> populateTextFields()));
       destination.valueProperty().addListener(((ov, oldValue, newValue) -> populateTextFields()));

    }



    @FXML
    void calculateRoute(){
        mainView.setImage(map);
        mainList.getItems().clear();
        if(multiple.isSelected()) {
            paths = findAllPathsDepthFirst(getRoom(source.getValue()), null, getRoom(destination.getValue()));
            List<List<CoolNode<Room>>> temp = new ArrayList<>();
            if(!waypoints.getSelectionModel().isEmpty()) {
                for (List<CoolNode<Room>> path : paths) {
                    if (path.contains(getRoom(waypoints.getSelectionModel().getSelectedItem()))) {
                        temp.add(path);
                    }
                }
                paths = temp;
            }
            paths.sort(Comparator.comparing(List::size));
            int limit = (int) routeLimit.getValue();
            if (paths.size() > limit)
                paths = paths.subList(0, limit);
            for (int i = 0; i < paths.size(); ++i) {
                mainList.getItems().add("Route: " + (i + 1));
            }
        }else{
            mainList.getItems().clear();
            int width = (int) blackAndWhite.getWidth(), height = (int) blackAndWhite.getHeight();
//            Room sourceRoom = getRoom(source.getValue()).getContents(), destinationRoom = getRoom(destination.getValue()).getContents();
            if(algorithmType.getValue().equals("Dijkstra's Algorithm")) {
                List<CoolNode<Room>> route = findCheapestPathDijkstra(getRoom(source.getValue()), getRoom(destination.getValue()).getContents()).getList();
                drawRoute(route);
            }else{
                List<Integer> route = findPathBreadthFirst(getRoom(source.getValue()).getContents().getPixelY()*width+getRoom(source.getValue()).getContents().getPixelX(),
                        getRoom(destination.getValue()).getContents().getPixelY()*width+getRoom(destination.getValue()).getContents().getPixelX());
               for(int i: route){
                   WritableImage wr = new WritableImage(mainView.getImage().getPixelReader(),width,height);
                   wr.getPixelWriter().setColor(i%width,i/width,Color.RED);
                   mainView.setImage(wr);
               }
            }



        }

    }

    private void drawRoute(List<CoolNode<Room>> route) {

        mainView.setImage(map);
        routeDetails.getItems().clear();
        routeDetails.getItems().add(0+": "+ route.get(0).getContents().getName());

        routeDetails.getItems().clear();
        routeDetails.getItems().add(0+": "+route.get(0).getContents().getName());

        for(int i = 1; i< route.size(); ++i){
            Room current = route.get(i).getContents(), prev = route.get(i-1).getContents();
            mainView.setImage(Utilities.drawLine(mainView.getImage(),current.getPixelX(),current.getPixelY(),prev.getPixelX(),prev.getPixelY(), Color.BLUE));
            mainView.setImage(Utilities.drawLine(mainView.getImage(),prev.getPixelX(),prev.getPixelY(),current.getPixelX(),current.getPixelY(),Color.BLUE));
            routeDetails.getItems().add(i+": "+current.getName());
        }
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


    //Interface method to allow just the starting node and the goal node data to match to be specified
    public List<Integer> findPathBreadthFirst(Integer startNode, Integer lookingfor){
        List<List<Integer>> agenda=new ArrayList<>(); //Agenda comprised of path lists here!
        List<Integer> firstAgendaPath=new ArrayList<>(),resultPath;
        firstAgendaPath.add(startNode);
        agenda.add(firstAgendaPath);
        resultPath=findPathBreadthFirst(agenda,null,lookingfor); //Get single BFS path (will be shortest)
        Collections.reverse(resultPath); //Reverse path (currently has the goal node as the first item)
        return resultPath;
    }
    //Agenda list based breadth-first graph search returning a single reversed path (tail recursive)
    public List<Integer> findPathBreadthFirst(List<List<Integer>> agenda, List<Integer> encountered, Integer lookingfor){

        if(agenda.isEmpty()) return null; //Search failed
        List<Integer> nextPath=agenda.remove(0); //Get first item (next path to consider) off agenda
        Integer currentNode=nextPath.get(0); //The first item in the next path is the current node
        


            if (currentNode.equals(lookingfor))
                return nextPath; //If that's the goal, we've found our path (so return it)
            if (encountered == null)
                encountered = new ArrayList<>(); //First node considered in search so create new (empty)
            //  encountered list
        if(!encountered.contains(currentNode)) {
      //      System.out.println(currentNode);
//        WritableImage wr = new WritableImage(mainView.getImage().getPixelReader(),(int) map.getWidth(),(int) map.getHeight());
//        wr.getPixelWriter().setColor(currentNode%((int) map.getWidth()),currentNode/((int) map.getWidth()),Color.RED);
//        mainView.setImage(wr);
            encountered.add(currentNode); //Record current node as encountered so it isn't revisited again
            int[] directions = {1, -1, (int) map.getWidth(), -((int) map.getWidth())};
            for (int direction : directions) {//For each adjacent node
                int adjNode = currentNode + direction;
                if (blackAndWhiteArray[adjNode] != 0 && !encountered.contains(adjNode)) { //If it hasn't already been encountered
                    List<Integer> newPath = new ArrayList<>(nextPath); //Create a new path list as a copy of the current/next path
                    newPath.add(0, adjNode); //And add the adjacent node to the front of the new copy
                    agenda.add(newPath); //Add the new path to the end of agenda (end->BFS!)
                }
            }
        }
        return findPathBreadthFirst(agenda,encountered,lookingfor); //Tail call
    }

    public static  CostedPath findCheapestPathDijkstra(CoolNode<Room> startNode, Room lookingfor){

        CostedPath cp=new CostedPath(); //Create result object for cheapest path
        List<CoolNode<Room>> encountered=new ArrayList<>(), unencountered=new ArrayList<>(); //Create encountered/unencountered lists
        startNode.setNodeValue(0); //Set the starting node value to zero
        unencountered.add(startNode); //Add the start node as the only value in the unencountered list to start
        CoolNode<Room> currentNode;
        do{ //Loop until unencountered list is empty
            currentNode=unencountered.remove(0); //Get the first unencountered node (sorted list, so will have lowest value)
            encountered.add(currentNode); //Record current node in encountered list
            if(currentNode.getContents().equals(lookingfor)){ //Found goal - assemble path list back to start and return it
                cp.getList().add(currentNode); //Add the current (goal) node to the result list (only element)
                cp.setCost(currentNode.getNodeValue()); //The total cheapest path cost is the node value of the current/goal node
                while(currentNode!=startNode) { //While we're not back to the start node...
                    boolean foundPrevPathNode=false; //Use a flag to identify when the previous path node is identified
                    for(CoolNode<Room> n : encountered) { //For each node in the encountered list...
                        for(CoolNode<Room> e : n.getAttachedNodes()){ //For each edge from that node...
                            if(e.equals(currentNode) && Math.abs(currentNode.getNodeValue()-(Utilities.distance(
                                    n.getContents().getPixelX(),n.getContents().getPixelY(),e.getContents().getPixelX(),
                                    e.getContents().getPixelY())))==n.getNodeValue()){ //If that edge links to the
//current node and the difference in node values is the cost of the edge -> found path node!

                                cp.getList().add(0,n); //Add the identified path node to the front of the result list
                                currentNode=n; //Move the currentNode reference back to the identified path node
                                foundPrevPathNode=true; //Set the flag to break the outer loop
                                break; //We've found the correct previous path node and moved the currentNode reference
//back to it so break the inner loop
                            }}
                        if(foundPrevPathNode) break; //We've identified the previous path node, so break the inner loop to continue
                    }
                }
//Reset the node values for all nodes to (effectively) infinity so we can search again (leave no footprint!)
                for(CoolNode<Room> n : encountered) n.setNodeValue(Integer.MAX_VALUE);
                for(CoolNode<Room> n : unencountered) n.setNodeValue(Integer.MAX_VALUE);
                return cp; //The costed (cheapest) path has been assembled, so return it!
            }
//We're not at the goal node yet, so...
            for(CoolNode<Room> e : currentNode.getAttachedNodes()) //For each edge/link from the current node...
                if(!encountered.contains(e)) { //If the node it leads to has not yet been encountered (i.e. processed)
                    e.setNodeValue(Integer.min(e.getNodeValue(), (currentNode.getNodeValue()+Utilities.distance
                                                (currentNode.getContents().getPixelX(),currentNode.getContents().getPixelY(),
                                                        e.getContents().getPixelX(),e.getContents().getPixelY()))));//Update the node value at the end
//of the edge to the minimum of its current value or the total of the current node's value plus the cost of the edge
                    unencountered.add(e);
                }
            unencountered.sort(Comparator.comparingInt(CoolNode::getNodeValue)); //Sort in ascending node value order
        }while(!unencountered.isEmpty());
        return null; //No path found, so return null
    }

    public void selectPixel(MouseEvent e){
        int pixelX, pixelY;
        pixelX = (int) e.getX();
        pixelY = (int) e.getY();

        sourceButton.setOnAction(event -> {
            currentTextField = sourceTextField;
        });
        destinationButton.setOnAction(event -> {
            currentTextField = destinationTextField;
        });

        if (currentTextField == sourceTextField){
            sourceTextField.setText("X = " + pixelX + " Y = " + pixelY);
        } else if(currentTextField == destinationTextField){
            destinationTextField.setText("X = " + pixelX + " Y = " + pixelY);
        }
        currentTextField = null;
    }



    public void populateTextFields() {

        if (algorithmType.getValue() == "Breadth First Search Algorithm") {
            source.valueProperty().addListener(e -> {
                if (source.getValue() != null) {
                    int pixelX, pixelY;

                    String sourceRoomName = source.getValue();
                    CoolNode<Room> room = getRoom(sourceRoomName);

                    pixelX = room.getContents().getPixelX();
                    pixelY = room.getContents().getPixelY();
                    // System.out.println("X = " + pixelX + " Y = " + pixelY);

                    sourceTextField.setText("X = " + pixelX + " Y = " + pixelY);
                }
            });

            destination.valueProperty().addListener(e -> {
                int pixelX, pixelY;

                if (destination.getValue() != null) {
                    String destinationRoomName = destination.getValue();
                    CoolNode<Room> room = getRoom(destinationRoomName);

                    pixelX = room.getContents().getPixelX();
                    pixelY = room.getContents().getPixelY();

                    destinationTextField.setText("X = " + pixelX + " Y = " + pixelY);
                } else destinationTextField.clear();
            });


        }
    }

   


    public void save(Map<Integer,CoolNode<Room>> savedItem, String fileName) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        ObjectOutputStream out = xstream.createObjectOutputStream(new FileWriter(fileName));
        out.writeObject(savedItem);
        out.close();
    }


}
