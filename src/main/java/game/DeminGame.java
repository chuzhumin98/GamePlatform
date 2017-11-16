package game;

import java.io.IOException;
import java.util.ArrayList;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import websocket.WebSocket;
import player.Player;

public class DeminGame extends GameState{
	int gridLen = 8; //雷区的大小
	int[][] gridState = new int [gridLen][gridLen]; //0表示还未有人选中，1表示已选过，2表示正在有人选中
	DeminGame() {
		super();
		this.gameType = 1; //扫雷为类型1
		this.maxTurnTime = 30; //扫雷游戏设置的一轮游戏时间为30s
		this.leftTime = new Integer(this.maxTurnTime);
		for (int i = 0; i < gridLen; i++) {
			for (int j= 0; j < gridLen; j++) {
				gridState[i][j] = 0; 
			}
		}
	}
	public void HandleDemin(int clickX, int clickY, int clickType, WebSocket ws) { //完成扫雷游戏中的用户响应
		//System.out.println("leftTime: "+ this.leftTime);
		if (this.isStarted && !ws.myPlayer.hasClicked && this.gridState[clickX][clickY] != 1) { //该方格还未选过时响应该次选方格
			ws.myPlayer.clickX = clickX;
			ws.myPlayer.clickY = clickY;
			if (clickType == 0) {
				ws.myPlayer.clickType = true;
			} else {
				ws.myPlayer.clickType = false;
			}
			ws.myPlayer.hasClicked = true; //完成该轮扫雷工作
			this.finshedNum++;
			if (this.finshedNum < this.gameNum) {
				sendForGameProcess(); //该轮未结束时告知用户进展情况
			} else {
				this.batchHandleTurn();
				sendEndOfThisTurn(); //完成此轮后进行一些常规操作
			}
		}	
	}
	public void sendForGameProcess() {
		JSONObject json1 = new JSONObject();
		json1.put("action", 3); //3表示扫雷该轮仍处于进行状态
		json1.put("finished", 1); //1表示已经完成
		json1.put("playerNum", players.size());
		json1.put("finishNum", this.finshedNum);
		json1.put("leftTime", this.leftTime); //发送本轮剩余时间
		String messages1 = json1.toString();
		json1.put("finished", 0);
		String messages2 = json1.toString();
		for (Player item : players) {
			try {
				//System.out.println(item.username);
				if (item.myWebsocket != null) {
					if (item.hasClicked) {
						item.myWebsocket.session.getBasicRemote().sendText(messages1);
					} else {
						item.myWebsocket.session.getBasicRemote().sendText(messages2);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void batchHandleTurn() { //当前轮结束时进行批处理
		for (Player item : players) {
			int type = 2;
			if (item.clickType) {
				type = 1;
			}
			this.gridState[item.clickX][item.clickY] = type;
			item.hasClicked = false;
		}
		synchronized (this.leftTime) {
			this.leftTime = this.maxTurnTime;
		}
		this.finshedNum = 0; //完成人数置为0
	}
	public void sendEndOfThisTurn() {	
		JSONObject json1 = new JSONObject();
		json1.put("action", 4); //4表示扫雷该轮已经结束
		ArrayList<ArrayList> arr = new ArrayList<ArrayList>();
		for (int i = 0; i < gridLen; i++) {
			ArrayList<Integer> arr1 = new ArrayList<Integer>();
			for (int j = 0; j < gridLen; j++) {
				arr1.add(this.gridState[i][j]);
			}
			arr.add(arr1);
		}
		json1.put("state", arr);
		json1.put("leftTime", this.leftTime);
		String messages = json1.toString();
		for (Player item : players) {
			try {
				if (item.myWebsocket != null) {
					item.myWebsocket.session.getBasicRemote().sendText(messages);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void revisiting(Player ply) { //处理有玩家回到游戏中的情况
		if (!this.isStarted) return; //游戏还没开始时忽略请求
		for (Player item: this.players) {
			if (item.username.equals(ply.username)) {
				System.out.println("Has send!");
				this.sendEndOfThisTurn();
			} else {
				System.out.println("No send!");
			}
		}
	}
}