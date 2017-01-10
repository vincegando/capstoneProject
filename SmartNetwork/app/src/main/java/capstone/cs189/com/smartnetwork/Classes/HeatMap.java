package capstone.cs189.com.smartnetwork.Classes;

import java.util.ArrayList;

public class HeatMap {

    private String router;
    private String address;
    private String date;
    private ArrayList<TestPoint> testPointList;
    private ArrayList<RouterPoint> routerPointList;

    public HeatMap() {

    }

    public HeatMap(ArrayList<TestPoint> testList, ArrayList<RouterPoint> routerList) {
        testPointList = testList;
        routerPointList = routerList;
    }

    public void addTestPin(TestPoint testPoint) {
        testPointList.add(testPoint);
    }

    public void addRouterPin(RouterPoint routerPoint) {
        routerPointList.add(routerPoint);
    }

    public void removeTestPin(TestPoint testPoint) {
        routerPointList.remove(testPoint);
    }

    public void removeRouterPin(RouterPoint routerPoint) {
        routerPointList.remove(routerPoint);
    }

}
