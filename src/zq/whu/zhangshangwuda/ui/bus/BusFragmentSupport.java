package zq.whu.zhangshangwuda.ui.bus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zq.whu.zhangshangwuda.base.BaseSherlockFragment;
import zq.whu.zhangshangwuda.ui.BuildConfig;
import zq.whu.zhangshangwuda.ui.R;
import zq.whu.zhangshangwuda.ui.bus.ChooseBusLineFragment.MyInterface;
import zq.whu.zhangshangwuda.ui.bus.data.Data_daxunhuan;
import zq.whu.zhangshangwuda.ui.bus.data.Data_gong_shitang;
import zq.whu.zhangshangwuda.ui.bus.data.Data_gong_xiaomen;
import zq.whu.zhangshangwuda.ui.bus.data.Data_station_daxunhuan;
import zq.whu.zhangshangwuda.ui.bus.data.Data_station_gong_men;
import zq.whu.zhangshangwuda.ui.bus.data.Data_station_gong_shi;
import zq.whu.zhangshangwuda.ui.bus.data.Data_station_wen_hu;
import zq.whu.zhangshangwuda.ui.bus.data.Data_station_wen_men;
import zq.whu.zhangshangwuda.ui.bus.data.Data_wenli_hubin;
import zq.whu.zhangshangwuda.ui.bus.data.Data_wenli_xiaomen;
import zq.whu.zhangshangwuda.views.toast.ToastUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

public class BusFragmentSupport extends BaseSherlockFragment implements
		OnClickListener, MyInterface, OnMarkerClickListener, OnMapClickListener {

	private Fragment fragment;
	private Timer timer = null;
	private FragmentManager manager;
	private InfoWindow mInfoWindow;

	private final String baseURL = "http://115.29.17.73:12334/";
	private final String routeUrl = baseURL + "bus/get_bus_info";
	private final String KEY_lineId = "lineId";
	private final static int NET_CONNECT_FAILL = 0x00ff0f01;
	private final static int BUS_UPDATE = 0x00ff0f02;
	private final static int BUS_STATUS_0 = 0x00ff0f03;
	private final static String CHOOSE_LINE_TAG = "choose_paper";

	private int[] colorList = { 0xaaff0000, 0xaa00ff00, 0xaa0000ff, 0xaaffff00,
			0xaa00ffff };
	private int lineName[] = { R.string.gong_men, R.string.gong_shi,
			R.string.wen_men, R.string.wen_hu, R.string.daxunhuan };

	private Map<String, Marker> busMarkers = new HashMap<String, Marker>();
	private List<LatLng> lineData = null;
	private List<LatLng> stationData = null;
	private List<String> stationName = null;

	static Activity mActivity;
	static BusFragmentSupport mFragment;

	private BaiduMap baiduMap;
	private MapView mapView;
	private View rootView;
	private LinearLayout menuBar;
	private ImageButton dropBtn;
	private Button changeLine;
	private Button stationRemind;
	private Button firstChoose;
	private TextView remindShow;
	private TextView busLineShow;
	private RelativeLayout mainBar;
	private RelativeLayout mainFace;

	private BitmapDescriptor finalPoint = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_final_point);
	private BitmapDescriptor busIcon = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_bus);
	private BitmapDescriptor stationNotReach = BitmapDescriptorFactory
			.fromResource(R.drawable.icon_p_not_reach);

	private int lineId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		manager = getChildFragmentManager();
		mActivity = getActivity();
		mFragment = this;
		Data_daxunhuan.init();
		Data_gong_shitang.init();
		Data_gong_xiaomen.init();
		Data_wenli_hubin.init();
		Data_wenli_xiaomen.init();
		Data_station_daxunhuan.init();
		Data_station_gong_men.init();
		Data_station_gong_shi.init();
		Data_station_wen_hu.init();
		Data_station_wen_men.init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.bus, container, false);
		initMap();
		initView();
		if (savedInstanceState != null) {
			lineId = savedInstanceState.getInt(KEY_lineId);
			busMarkers.clear();
			baiduMap.clear();
			drawLine(lineId);
			mainBar.setVisibility(View.VISIBLE);
			firstChoose.setVisibility(View.GONE);
			if (timer != null)
				timer.cancel();
			timer = new Timer(true);
			timer.schedule(new BusPostion(getRouteIDFromLineId(lineId)), 0, 5000);
		} else {
			baiduMap.clear();
			drawLine(0);
			drawLine(1);
			drawLine(2);
			drawLine(3);
			drawLine(4);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {
		mapView = (MapView) rootView.findViewById(R.id.bmapView);
		baiduMap = mapView.getMap();
		MapStatus status = new MapStatus.Builder(baiduMap.getMapStatus())
				.target(ConstantPos.jianhu).zoom(16).build();
		MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(status);
		baiduMap.setMapStatus(update);

		baiduMap.setOnMarkerClickListener(this);
		baiduMap.setOnMapClickListener(this);

		mapView.getChildAt(2).setVisibility(View.GONE);// 隐藏缩放控件
		mapView.getChildAt(3).setVisibility(View.GONE); // 隐藏比例尺控件
		// mapView.removeViewAt(1); // 删除百度地图logo

	}

	/**
	 * 初始化各个控件
	 */
	private void initView() {
		mainBar = (RelativeLayout) rootView.findViewById(R.id.main_bar);
		mainBar.getBackground().setAlpha(0xDD);
		mainBar.setVisibility(View.GONE);

		menuBar = (LinearLayout) rootView.findViewById(R.id.menuBar);

		dropBtn = (ImageButton) rootView
				.findViewById(R.id.main_bar_drop_button);
		dropBtn.setOnClickListener(this);

		changeLine = (Button) rootView.findViewById(R.id.lineChange);
		changeLine.setOnClickListener(this);

		stationRemind = (Button) rootView.findViewById(R.id.stationRemind);
		stationRemind.setOnClickListener(this);

		remindShow = (TextView) rootView.findViewById(R.id.remindShow);

		mainFace = (RelativeLayout) rootView.findViewById(R.id.main_face);

		firstChoose = (Button) rootView.findViewById(R.id.firstChoose);
		firstChoose.setOnClickListener(this);

		busLineShow = (TextView) rootView.findViewById(R.id.bus_line_show);
	}

	/**
	 * 根据客户端的线路id返回route_Id用于向服务器查询
	 * 
	 * @param lineId
	 * @return
	 */
	private int getRouteIDFromLineId(int lineId) {
		switch (lineId) {
		case 0 | 1:
			return 2;
		case 2 | 3:
			return 4;
		case 4:
			return 6;
		default:
			return 2;
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case NET_CONNECT_FAILL:// 网络问题
				if (BuildConfig.DEBUG) {
					System.out.println(R.string.Bus_Connect_Tip);
				}
				ToastUtil.showToast(mActivity, R.string.No_Intenert_Tip,
						Toast.LENGTH_LONG);
				break;

			case BUS_UPDATE:// 正常更新
				JSONArray busList = (JSONArray) msg.obj;
				busListUpdate(msg.arg1, busList);
				break;

			case BUS_STATUS_0:// 返回数据status为0
				if (BuildConfig.DEBUG) {
					System.out.println("BUS_STATUS_0");
				}
				ToastUtil.showToast(mActivity, R.string.Bus_Status_0_Tip,
						Toast.LENGTH_LONG);
				break;
			}
		}

		/**
		 * 更新routeId路线上的校车
		 * 
		 * @param routeId
		 * @param busList
		 */
		private void busListUpdate(int routeId, JSONArray busList) {
			if (busList == null)
				return;
			for (int i = 0; i < busList.length(); i++) {
				JSONObject bus = busList.optJSONObject(i);
				if (bus != null) {
					BusInfo busInfo = new BusInfo(bus);
					updateOneBus(routeId, busInfo);
				}
			}
		}

		private void updateOneBus(int routeId, BusInfo busInfo) {

			Log.i("BUSINFO", busInfo.latitude + ", " + busInfo.longitude);
			Marker marker = busMarkers.get("" + busInfo.id);
			if (marker == null) {
				OverlayOptions overlay = new MarkerOptions()
						.icon(busIcon)
						.position(
								new LatLng(busInfo.latitude, busInfo.longitude))
						.zIndex(9);
				marker = (Marker) baiduMap.addOverlay(overlay);
				busMarkers.put(busInfo.id + "", marker);
			} else {
				marker.setPosition(new LatLng(busInfo.latitude,
						busInfo.longitude));
			}

		}
	};

	private class BusPostion extends TimerTask {
		int routeId = 2;

		BusPostion(int routeId) {
			this.routeId = routeId;
		}

		@Override
		public void run() {
			getBusPos();
		}

		private void getBusPos() {
			boolean flag = false;// 是否连接成功的标志
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(routeUrl + "?route_id=" + routeId);
			try {
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse == null) {
					System.out.println("getBusPos no reponse");
				} else {
					int state = httpResponse.getStatusLine().getStatusCode();
					if (state == HttpStatus.SC_OK) {
						flag = true;
						HttpEntity httpEntity = httpResponse.getEntity();
						String responseStr = EntityUtils.toString(httpEntity);
						if (BuildConfig.DEBUG)
							Log.i("BUSINFO", responseStr);
						getBusPosList(routeId, responseStr);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				System.out.println(e);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				httpClient.getConnectionManager().shutdown();
				if (!flag) {
					Message message = new Message();
					message.what = NET_CONNECT_FAILL;
					handler.sendMessage(message);
				}
			}
		}

		private void getBusPosList(int routeId, String busStr) {
			try {
				JSONObject all = new JSONObject(busStr);
				int flag = all.getInt("status");
				if (flag == 0) {
					Message message = new Message();
					message.what = BUS_STATUS_0;
					handler.sendMessage(message);
					return;
				}
				JSONArray busList = all.optJSONArray("bus_info");

				Message message = new Message();
				message.what = BUS_UPDATE;
				message.arg1 = routeId;
				message.obj = (Object) busList;
				handler.sendMessage(message);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.main_bar_drop_button:
			if (menuBar.getVisibility() == View.GONE) {
				menuBar.setVisibility(View.VISIBLE);
				dropBtn.setImageResource(R.drawable.btn_down_1);
			} else {
				menuBar.setVisibility(View.GONE);
				dropBtn.setImageResource(R.drawable.btn_drop_1);
			}
			break;

		case R.id.lineChange:
			if (menuBar.getVisibility() == View.VISIBLE) {
				menuBar.setVisibility(View.GONE);
				dropBtn.setImageResource(R.drawable.btn_drop_1);
			}
			// mainFace.setVisibility(View.GONE);
			showChoosePaper();
			break;

		case R.id.stationRemind:
			if (menuBar.getVisibility() == View.VISIBLE) {
				menuBar.setVisibility(View.GONE);
				dropBtn.setImageResource(R.drawable.btn_drop_1);
			}
			break;

		case R.id.firstChoose:
			showChoosePaper();
			firstChoose.setVisibility(View.GONE);
			mainBar.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void showChoosePaper() {
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		if (fragment == null) {
			fragment = manager.findFragmentByTag(CHOOSE_LINE_TAG);
		}

		if (fragment == null) {
			fragment = new ChooseBusLineFragment();
		}

		transaction.add(R.id.chooseFL, fragment, CHOOSE_LINE_TAG);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onLineItemSelected(int lineId) {

		// mainFace.setVisibility(View.VISIBLE);
		this.lineId = lineId;
		busMarkers.clear();
		baiduMap.clear();
		drawLine(lineId);

		if (fragment == null)
			fragment = manager.findFragmentByTag(CHOOSE_LINE_TAG);
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.remove(fragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		transaction.commit();

		if (timer != null)
			timer.cancel();
		timer = new Timer(true);
		timer.schedule(new BusPostion(getRouteIDFromLineId(lineId)), 0, 5000);
	}

	private void drawLine(int lineId) {
		switch (lineId) {
		case 0:
			lineData = Data_gong_xiaomen.gong_xiaomen;
			stationData = Data_station_gong_men.station;
			stationName = Data_station_gong_men.stationName;
			break;
		case 1:
			lineData = Data_gong_shitang.gong_shitang;
			stationData = Data_station_gong_shi.station;
			stationName = Data_station_gong_shi.stationName;
			break;
		case 2:
			lineData = Data_wenli_xiaomen.wenli_xiaomen;
			stationData = Data_station_wen_men.station;
			stationName = Data_station_wen_men.stationName;
			break;
		case 3:
			lineData = Data_wenli_hubin.wenli_hubin;
			stationData = Data_station_wen_hu.station;
			stationName = Data_station_wen_hu.stationName;
			break;
		case 4:
			lineData = Data_daxunhuan.daxunhuan;
			stationData = Data_station_daxunhuan.station;
			stationName = Data_station_daxunhuan.stationName;
			break;
		}
		busLineShow.setText(lineName[lineId]);

		OverlayOptions way = new PolylineOptions().color(colorList[lineId])
				.points(lineData).width(10);
		baiduMap.addOverlay(way);

		Bundle bs = new Bundle();
		bs.putString("station", stationName.get(0));
		OverlayOptions startPoint = new MarkerOptions().icon(finalPoint)
				.position(stationData.get(0)).zIndex(9).extraInfo(bs);
		baiduMap.addOverlay(startPoint);

		Bundle be = new Bundle();
		be.putString("station", stationName.get(stationName.size() - 1));
		OverlayOptions endPoint = new MarkerOptions().icon(finalPoint)
				.position(stationData.get(stationData.size() - 1)).zIndex(9)
				.extraInfo(be);
		baiduMap.addOverlay(endPoint);

		for (int i = 1; i < stationData.size() - 1; i++) {
			Bundle b = new Bundle();
			b.putString("station", stationName.get(i));
			OverlayOptions oo = new MarkerOptions().icon(stationNotReach)
					.position(stationData.get(i)).zIndex(9).extraInfo(b);
			baiduMap.addOverlay(oo);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		TextView info = new TextView(getActivity());
		String infoStr = marker.getExtraInfo().getString("station");
		if (infoStr != null)
			info.setText(infoStr);
		final LatLng ll = marker.getPosition();
		Point p = baiduMap.getProjection().toScreenLocation(ll);
		p.y -= 47;
		LatLng llInfo = baiduMap.getProjection().fromScreenLocation(p);
		OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
			public void onInfoWindowClick() {
				baiduMap.hideInfoWindow();
			}
		};
		mInfoWindow = new InfoWindow(info, llInfo, listener);
		baiduMap.showInfoWindow(mInfoWindow);
		return true;
	}

	@Override
	public void onDestroy() {
		mapView.onDestroy();
		if (timer != null)
			timer.cancel();
		super.onDestroy();
		finalPoint.recycle();
		busIcon.recycle();
		stationNotReach.recycle();
	}

	@Override
	public void onPause() {
		mapView.onPause();
		Bundle outState = new Bundle();
		outState.putInt(KEY_lineId, lineId);
		onSaveInstanceState(outState);
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		System.out.println("onSaveInstanceState  ");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onMapClick(LatLng arg0) {
		baiduMap.hideInfoWindow();
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
	}

}