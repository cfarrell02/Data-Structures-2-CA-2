package Main;

public class Room {
    private String name, details;
    private int ID,pixelX,pixelY;

    public Room(String name, String details, int ID,int pixelX,int pixelY) {
        this.name = name;
        this.details = details;
        this.ID = ID;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getPixelX() {
        return pixelX;
    }

    public void setCoords(int pixelX,int pixelY) {
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

    public int getPixelY() {
        return pixelY;
    }


//    public boolean equals(Room otherRoom){
//        return otherRoom.getID() == this.ID;
//    }
    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", details='" + details + '\'' +
                ", ID=" + ID +
                ", pixelX=" + pixelX +
                ", pixelY=" + pixelY +
                '}';
    }
}


