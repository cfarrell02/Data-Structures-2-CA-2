package Main;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.List;

public class Utilities {

    public static Image drawX(Image image, int x, int y,Color color){
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage newImage = new WritableImage(image.getPixelReader(),width,height);
        PixelWriter pw = newImage.getPixelWriter();
        int size = 4;
        int lineWidth = 1;
        int x2,y2;
        for(int i = -size;i<=size;i++) {
            for(int j = -lineWidth;j<=lineWidth;j++){
                x2 = x + i;
                y2 = y + i;
                if(j%2==0)
                    x2-=j;
                else
                    y2+=j;

                if (x2 >= 0 && y2 >= 0 && x2 < width && y2 < height) pw.setColor(x2, y2, color);
                x2 = x - i;
                if(j%2==0)x2+=j;
                if (x2 >= 0 && y2 >= 0 && x2 < width && y2< height) pw.setColor(x2, y2, color);
            }
        }
        return newImage;
    }
    public static Image drawLine(Image image,int x1, int y1,int x2,int y2,Color color){

        int width = (int) image.getWidth(), height = (int) image.getHeight();
        WritableImage newImage = new WritableImage(image.getPixelReader(),width,height);
        if(x1==x2){
            for(int y = 0;y< height;++y){
                if((y>=y1&&y<=y2)) newImage.getPixelWriter().setColor(x1,y,color);
            }
        }else if(y1==y2){
            for(int x = 0;x< width;++x){
                if((x>=x1&&x<=x2)) newImage.getPixelWriter().setColor(x,y2,color);
            }
        }else{

        double slope = (y2-y1)/(x2-x1);
        // y = y1 + m.x - m.x1 / x = (y-y1+m.x1)/m
        if(Math.abs(slope)<1) {
            for (int x = 0; x < width; ++x) {
                if ((x >= x1 && x <= x2)) {
                    newImage.getPixelWriter().setColor(x, y1 + ((y2-y1) * (x - x1))/(x2-x1), color);
                }
            }
        }else{
            for (int y = 0; y < height; ++y) {
                if ((y >= y1 && y <= y2)) {
                    newImage.getPixelWriter().setColor( ((y-y1)*(x2-x1)/(y2-y1)+x1), y, color);

                }
            }
        }
        }
        return newImage;

    }

    public static Color randomColor(){
        return new Color(Math.random(),Math.random(),Math.random(),1);
    }

    public static int distance(int x1, int y1, int x2, int y2){
        return (int) Math.round(Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2)));
    }
}
