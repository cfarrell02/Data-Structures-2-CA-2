package Main;

import java.util.ArrayList;
import java.util.List;

class CoolNode<N>{
    private List<CoolNode<N>> attachedNodes;
    private N contents; //ADT reference
    CoolNode(N data) {
        contents = data;
        attachedNodes = new ArrayList<>();
    }

    public void connectToNodeDirected(CoolNode<N> destNode) {
        attachedNodes.add(destNode);
    }
    public void connectToNodeUndirected(CoolNode<N> destNode) {
        attachedNodes.add(destNode);
        destNode.getAttachedNodes().add(this);
    }

    public void disconnectNodeDirected(CoolNode<N> destNode){
        attachedNodes.remove(destNode);
    }

    public void disconnectNodeUnDirected(CoolNode<N> destNode){
        attachedNodes.remove(destNode);
        destNode.attachedNodes.remove(destNode);
    }

    public List<CoolNode<N>> getAttachedNodes() {
        return attachedNodes;
    }

    public void setAttachedNodes(List<CoolNode<N>> attachedNodes) {
        this.attachedNodes = attachedNodes;
    }

    public N getContents() { return contents; }
    public void setContents(N c) { contents=c; }


    @Override
    public String toString() {
        return "CoolNode{" +
                "contents=" + contents.toString() +
                '}';
    }
}
