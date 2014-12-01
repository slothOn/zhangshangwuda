package zq.whu.zhangshangwuda.ui.bus.data;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.model.LatLng;

public class Data_station_wen_hu {
	public static List<LatLng> station = new ArrayList<LatLng>();
	public static List<String> stationName = new ArrayList<String>();
	
	public static void init() {
		stationName.add("湖滨");
		stationName.add("新传、历史学院");
		stationName.add("樱园");
		stationName.add("教五、教四、化院");
		stationName.add("校门（向南）");
		
		station.add(new LatLng(30.547646, 114.377202)); // 湖滨
		station.add(new LatLng(30.546176, 114.373518)); // 新传、历史学院
		station.add(new LatLng(30.545609, 114.369764)); // 樱园
		station.add(new LatLng(30.54277, 114.366974)); // 教五、教四、化院
		station.add(new LatLng(30.540803, 114.365982)); // 校门（向南）
	}
}