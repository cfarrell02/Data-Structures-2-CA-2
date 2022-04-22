package Main;

import java.util.ArrayList;
import java.util.List;

public class CostedPath {
    private List<CoolNode<Room>> list;
    private int cost;

    public CostedPath() {
        this.list = new ArrayList<>();
        this.cost = Integer.MAX_VALUE;
    }

    public List<CoolNode<Room>> getList() {
        return list;
    }

    public void setList(List<CoolNode<Room>> list) {
        this.list = list;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
