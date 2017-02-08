package capstone.cs189.com.smartnetwork.Classes;

import java.util.ArrayList;

public class HeatMap {

    private String createdDate;
    private ArrayList<TestPoint> testPointList;
    private ArrayList<RouterPoint> routerPointList;

    public HeatMap() {

    }

    public HeatMap(ArrayList<TestPoint> testList, ArrayList<RouterPoint> routerList, String date) {
        testPointList = testList;
        routerPointList = routerList;
        createdDate = date;
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

    public String getCreatedDate() {
        return createdDate;
    }

}
