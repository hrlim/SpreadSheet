package com.example.owner.spreadsheet.utils.listener;

import java.util.HashSet;
import java.util.Set;

public class ModelDataChangeListener {
    public static Set<IDataChangeable> dataChangeNotifySet = new HashSet<>();
    private static boolean blocked = false;

    public interface IDataChangeable {
        void dataChanged(int sectionType);
    }

    // Model 이 변경되었음을 통지 받기를 원하는 IDataChangeable 을 set 에 추가한다.
    public static void addDataChangeable(IDataChangeable dataChangeable) {
        boolean exist = false;
        // 같은 IDataChangeable 이 있는지 확인
        for (IDataChangeable check : dataChangeNotifySet) {
            if (check != dataChangeable) {
                exist = true;
                break;
            }
        }
        // 같은 IDataChangeable 이 없을 경우
        if (!exist) {
            dataChangeNotifySet.add(dataChangeable);
        }
    }

    // Model 이 변경되었음을 통지 받기를 해제하는 메소드
    public static void removeDataChangeable(IDataChangeable dataChangeable) {
        dataChangeNotifySet.remove(dataChangeable);
    }

    // set 에 있는 IDataChangeable 에게 Model 이 변경되었음을 알려주는 메소드
    public static void notifyDataChanged(int section) {
        for (IDataChangeable dataChangeable : dataChangeNotifySet) {
            if(!blocked) {
                if (dataChangeable != null) {
                    dataChangeable.dataChanged(section);
                }
            }
        }
    }

    // 불러오기와 같이 Model 이 변경될때마다 invalidate 를 시키지않도록 사용하는 메소드
    // 불러오기와 같은 작업이 끝나고 수행한다.
    // 이 메소드가 호출되면 invalidate 를 한다.
    public static void run(int section){
        blocked = false;
        notifyDataChanged(section);
    }

    // 불러오기와 같이 Model 이 변경될때마다 invalidate 를 시키지않도록 사용하는 메소드
    public static void run(){
        blocked = false;
    }

    // 불러오기와 같이 Model 이 변경될때마다 invalidate 를 시키지않도록 사용하는 메소드
    public static void pause(){
        blocked = true;
    }

}
