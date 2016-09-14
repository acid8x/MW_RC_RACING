package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnInitListener {

    private static final int MY_DATA_CHECK_CODE = 0;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_EXIT = 4;
    private static final int REQUEST_PLAYER_EDITOR = 5;
    private static final int REQUEST_RACE_SELECTION = 6;
    private static final int REQUEST_RESET = 7;
    private static final int REQUEST_WINNER = 8;

    static int raceType = 0;
    static boolean exitingState = false, winnerState = false;
    private String[] winnerResults = new String[]{"","","","",""};
    private int[] winnerArray = new int[]{-1,-1,-1,-1,-1};
    private int currentWinner = 0, ttsID = 0;
    private static int raceLapsNumber = 2, raceGatesNumber = 4, raceKillsNumber = 10;
    private static int exitingActivity = -1;
    private static List<Players> list = new ArrayList<>();
    private TextToSpeech tts;
    private ListView listView = null;
    private Context ctx = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private MainFragment mainFragment = null;
    private int connectedTrucks = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    receivedData(new String(readBuf, 0, msg.arg1));
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    Intent raceIntent = new Intent(MainActivity.this, RaceSelectionActivity.class);
                    raceIntent.putExtra("LAPS", raceLapsNumber);
                    raceIntent.putExtra("GATES", raceGatesNumber);
                    raceIntent.putExtra("KILLS", raceKillsNumber);
                    clearRaceType();
                    startActivityForResult(raceIntent, REQUEST_RACE_SELECTION);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (!MainActivity.this.isFinishing()) {
                        String error = msg.getData().getString(Constants.TOAST);
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        if ("Unable to connect device".equals(error) || ("Device connection was lost".equals(error) && !exitingState)) {
                            Intent listIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                            startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(mainFragment, "worker").commit();
            mainFragment.setRetainInstance(true);
        }

        ctx = this;
        listView = (ListView) findViewById( R.id.in);
        Collections.sort(list);
        listView.setAdapter( new ConstructorListAdapter(ctx, com.example.android.bluetoothchat.R.layout.listview_row_item, list ) );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Players o = (Players) parent.getItemAtPosition(position);
                int Id = o.getId();
                String Name = o.getName();
                int Red = o.getTruckColorR();
                int Green = o.getTruckColorG();
                int Blue = o.getTruckColorB();
                Intent serverIntent = new Intent(MainActivity.this, PlayerEditorActivity.class);
                serverIntent.putExtra("ID", Id);
                serverIntent.putExtra("NAME", Name);
                serverIntent.putExtra("RED", Red);
                serverIntent.putExtra("GREEN", Green);
                serverIntent.putExtra("BLUE", Blue);
                startActivityForResult(serverIntent, REQUEST_PLAYER_EDITOR);
            }
        });

        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resetIntent = new Intent(MainActivity.this, ResetActivity.class);
                startActivityForResult(resetIntent, REQUEST_RESET);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    private void setupChat() {
        mChatService = new BluetoothChatService(mHandler);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    private void sendMessage(String message) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        message += '\r';
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    private void clearRaceType() {
        winnerState = false;
        mainFragment.attemptSend("clearGameStats", "");
        connectedTrucks = 0;
        winnerArray = new int[]{-1,-1,-1,-1,-1};
        winnerResults = new String[]{"","","","",""};
        currentWinner = 0;
        list.clear();
        listView.setAdapter(new ConstructorListAdapter(ctx, com.example.android.bluetoothchat.R.layout.listview_row_item, list));
    }

    private int updatePlayer(int id) {
        int returnedId = -1;
        for (int i = 0; i < list.size(); i++) {
            int getId = list.get(i).getId();
            if (id == getId) {
                returnedId = i;
            }
        }
        if (returnedId == -1) {
            list.add(new Players(id));
            connectedTrucks++;
            mainFragment.attemptSend("newTruck", id - 1);
            sendMessage(id + "C" + raceType);
            for (int i = 0; i < list.size(); i++) {
                int getId = list.get(i).getId();
                if (id == getId) {
                    returnedId = i;
                }
            }
        }
        return returnedId;
    }

    private void receivedData(String data) {
        char[] dataReceived = data.toCharArray();
        int len = dataReceived.length;
        if (len == 3) {
            int node = dataReceived[0] - 48;
            char command = dataReceived[1];
            int arg1 = dataReceived[2] - 48;
            dataHandle(node,command,arg1);
        }
        else Toast.makeText(this, "(E) Receive: " + data, Toast.LENGTH_SHORT).show();
    }

    private void dataHandle(int node, char command, int arg1) {
        boolean valid = true;
        if (raceType == 0) valid = false;
        if (raceType == 1 && command == 'D') valid = false;
        if (raceType == 3 && command == 'G') valid = false;
        boolean boolArg1 = arg1 == 1;
        int currentId = updatePlayer(node);
        if (currentId != -1 && valid && !winnerState) {
            switch (command) {
                case 'P':
                    String message = "";
                    message += node;
                    message += arg1;
                    int otherId = updatePlayer(arg1);
                    int cTotalGates = list.get(currentId).getTotalGates();
                    int oTotalGates = list.get(otherId).getTotalGates();
                    if (cTotalGates == oTotalGates && currentId > otherId) Collections.swap(list,currentId,otherId);
                    mainFragment.attemptSend("truckBack", message);
                    break;
                case 'Z':
                    sendMessage(node + "C" + raceType);
                    break;
                case 'T':
                    list.get(currentId).setTurboAvailable(boolArg1);
                    break;
                case 'S':
                    list.get(currentId).setGunAvailable(boolArg1);
                    break;
                case 'D':
                    if (raceType != 1) {
                        int updateTotalDeaths = list.get(currentId).getTotalDeaths() + 1;
                        list.get(currentId).setTotalDeaths(updateTotalDeaths);
                        String updateTotalKillsSender = arg1 + "K1";
                        String ttsString = "Truck number " + node + " has been shot by Truck number " + arg1;
                        String UTTERANCE_ID = "";
                        UTTERANCE_ID += ttsID++;
                        tts.speak(ttsString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                        receivedData(updateTotalKillsSender);
                    }
                    break;
                case 'K':
                    int updateTotalKills = list.get(currentId).getTotalKills() + 1;
                    list.get(currentId).setTotalKills(updateTotalKills);
                    if (raceType == 3 && updateTotalKills == raceKillsNumber) {
                        raceType = 0;
                        winnerState = true;
                        String winnerName = "Congratulations\n" + list.get(currentId).getName() + " #" + list.get(currentId).getId() + " win the battle !";
                        String UTTERANCE_ID = "";
                        UTTERANCE_ID += ttsID++;
                        tts.speak(winnerName, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                        Intent intent = new Intent(MainActivity.this, WinnerActivity.class);
                        intent.putExtra("WINNER", winnerName);
                        startActivityForResult(intent, REQUEST_WINNER);
                    }
                    break;
                case 'G':
                    if (raceType < 3) {
                        int mNextGate = list.get(currentId).getNextGate();
                        int mTotalGates = list.get(currentId).getTotalGates();
                        int mTotalLaps = list.get(currentId).getTotalLaps();
                        if (arg1 == mNextGate) {
                            if (arg1 == 1) list.get(currentId).setTotalLaps(mTotalLaps + 1);
                            if (mTotalLaps == (raceLapsNumber - 1) && mNextGate == 1) {
                                winnerArray[currentWinner++] = currentId;
                                String winnerName = "";
                                if (currentWinner == 1) winnerName += "Congratulations\n" + list.get(currentId).getName() + " #" + list.get(currentId).getId() + " win the race !";
                                else winnerName += list.get(currentId).getName() + " #" + list.get(currentId).getId() + " is in position number " + currentWinner;
                                String UTTERANCE_ID = "";
                                UTTERANCE_ID += ttsID++;
                                tts.speak(winnerName, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                                if (currentWinner >= connectedTrucks) {
                                    for (int i = 0; i < 5; i++) {
                                        if (winnerArray[i] != -1) winnerResults[i] = list.get(winnerArray[i]).getName() + " #" + list.get(winnerArray[i]).getId();
                                        else winnerResults[i] = "";
                                    }
                                    raceType = 0;
                                    winnerState = true;
                                    Intent intent = new Intent(MainActivity.this, WinnerActivity.class);
                                    intent.putExtra("P1", winnerResults[0]);
                                    intent.putExtra("P2", winnerResults[1]);
                                    intent.putExtra("P3", winnerResults[2]);
                                    intent.putExtra("P4", winnerResults[3]);
                                    intent.putExtra("P5", winnerResults[4]);
                                    startActivityForResult(intent, REQUEST_WINNER);
                                }
                                break;
                            }
                            if (mNextGate == raceGatesNumber) list.get(currentId).setNextGate(1);
                            else list.get(currentId).setNextGate(mNextGate + 1);
                            String msg = "";
                            msg += node;
                            msg += mNextGate;
                            mainFragment.attemptSend("gateUpdate", msg);
                            list.get(currentId).setTotalGates(mTotalGates + 1);
                            String ttsString = "Truck number " + node + " pass gate " + arg1;
                            String UTTERANCE_ID = "";
                            UTTERANCE_ID += ttsID++;
                            tts.speak(ttsString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                        }
                    }
                    break;
            }
            Collections.sort(list);
            listView.setAdapter( new ConstructorListAdapter(ctx, com.example.android.bluetoothchat.R.layout.listview_row_item, list ) );
            currentId = updatePlayer(node);
            mainFragment.attemptSend("truckUpdate", node-1, raceType, list.get(currentId).getTruckInfo() + "," + (currentId + 1) + "," + connectedTrucks + "," + raceType + "," + raceLapsNumber + "," + raceGatesNumber + "," + raceKillsNumber);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    tts = new TextToSpeech(this, this);
                    exitingActivity = -1;
                    Intent listIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                } else {
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
                break;
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == MainActivity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == DeviceListActivity.RESULT_OK) {
                    connectDevice(data, false);
                } else {
                    exitingActivity = REQUEST_CONNECT_DEVICE_INSECURE;
                    Intent serverIntent = new Intent(MainActivity.this, ExitActivity.class);
                    startActivityForResult(serverIntent, REQUEST_EXIT);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == MainActivity.RESULT_OK) {
                    setupChat();
                } else {
                    this.finish();
                }
                break;
            case REQUEST_EXIT:
                if (resultCode == MainActivity.RESULT_OK) {
                    finishAndRemoveTask();
                    System.exit(0);
                } else {
                    if (exitingActivity == REQUEST_CONNECT_DEVICE_INSECURE) {
                        exitingActivity = -1;
                        Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(intent, REQUEST_CONNECT_DEVICE_INSECURE);
                    } else if (exitingActivity == REQUEST_RACE_SELECTION) {
                        exitingActivity = -1;
                        Intent raceIntent = new Intent(MainActivity.this, RaceSelectionActivity.class);
                        raceIntent.putExtra("LAPS", raceLapsNumber);
                        raceIntent.putExtra("GATES", raceGatesNumber);
                        raceIntent.putExtra("KILLS", raceKillsNumber);
                        clearRaceType();
                        startActivityForResult(raceIntent, REQUEST_RACE_SELECTION);
                    } else if (exitingActivity == 0) {
                        exitingActivity = -1;
                    } else Toast.makeText(this, "(E) EXIT: " + exitingActivity, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PLAYER_EDITOR:
                if (resultCode == PlayerEditorActivity.RESULT_OK) {
                    int getId = data.getExtras().getInt("ID");
                    int currentId = updatePlayer(getId);
                    list.get(currentId).setName(data.getExtras().getString("NAME"));
                    list.get(currentId).setTruckColorR(data.getExtras().getInt("RED"));
                    list.get(currentId).setTruckColorG(data.getExtras().getInt("GREEN"));
                    list.get(currentId).setTruckColorB(data.getExtras().getInt("BLUE"));
                    Collections.sort(list);
                    listView.setAdapter(new ConstructorListAdapter(ctx, com.example.android.bluetoothchat.R.layout.listview_row_item, list));
                }
                break;
            case REQUEST_RACE_SELECTION:
                if (resultCode == MainActivity.RESULT_OK) {
                    raceType = data.getExtras().getInt("RACE_TYPE");
                    raceLapsNumber = data.getExtras().getInt("LAPS");
                    raceGatesNumber = data.getExtras().getInt("GATES");
                    raceKillsNumber = data.getExtras().getInt("KILLS");
                    mainFragment.attemptSend("gatesNumber", raceGatesNumber);
                    TextView rt = (TextView) findViewById(R.id.tvRaceType);
                    if (raceType == 1) {
                        rt.setText("RACE -NO- GUNS\n" + raceLapsNumber + " Laps with " + raceGatesNumber + " Gates");
                        String speechString = "Race " + raceLapsNumber + " laps, with " + raceGatesNumber + " gates per lap, without guns.";
                        String UTTERANCE_ID = "";
                        UTTERANCE_ID += ttsID++;
                        tts.speak(speechString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                    }
                    if (raceType == 2) {
                        rt.setText("RACE WITH GUNS\n" + raceLapsNumber + " Laps with " + raceGatesNumber + " Gates");
                        String speechString = "Race " + raceLapsNumber + " laps, with " + raceGatesNumber + " gates per lap, with guns.";
                        String UTTERANCE_ID = "";
                        UTTERANCE_ID += ttsID++;
                        tts.speak(speechString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                    }
                    if (raceType == 3) {
                        rt.setText("SEARCH & DESTROY\nFirst with " + raceKillsNumber + " Kills");
                        String speechString = "Battle game! First with " + raceKillsNumber + " kills, win the game.";
                        String UTTERANCE_ID = "";
                        UTTERANCE_ID += ttsID++;
                        tts.speak(speechString, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
                    }
                    if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                        Intent listIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(listIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                    }
                    sendMessage("1C0\r2C0\r3C0\r4C0\r5C0");
                } else {
                    exitingActivity = REQUEST_RACE_SELECTION;
                    Intent serverIntent = new Intent(MainActivity.this, ExitActivity.class);
                    startActivityForResult(serverIntent, REQUEST_EXIT);
                }
                break;
            case REQUEST_RESET:
                if (resultCode == MainActivity.RESULT_OK) {
                    exitingActivity = -1;
                    clearRaceType();
                    Intent intent = new Intent(MainActivity.this, RaceSelectionActivity.class);
                    startActivityForResult(intent, REQUEST_RACE_SELECTION);
                }
                break;
            case REQUEST_WINNER:
                list.clear();
                listView.setAdapter(new ConstructorListAdapter(ctx, com.example.android.bluetoothchat.R.layout.listview_row_item, list));
                exitingActivity = -1;
                clearRaceType();
                Intent intent = new Intent(MainActivity.this, RaceSelectionActivity.class);
                startActivityForResult(intent, REQUEST_RACE_SELECTION);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        exitingActivity = 0;
        Intent serverIntent = new Intent(MainActivity.this, ExitActivity.class);
        startActivityForResult(serverIntent, REQUEST_EXIT);
    }
}
