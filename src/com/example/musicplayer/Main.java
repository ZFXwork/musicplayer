package com.example.musicplayer;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;

public class Main extends Activity {
	private ImageButton next;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton stop;
	private ListView list1;
	private TextView time_now;
	private TextView time_total;
	private SeekBar seekbar;
	
	private int total;
	private int now;
	private Handler seekHandle;
	
	public static final int PROGESS_INCREASE=0;
	public static final int PROGESS_PAUSE=1;
	public static final int PROGESS_RESET=2;
	
	private int status;
	private int number;
	private StatuteChangedReceiver receiver;
	
	
	class StatuteChangedReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// 获取播放器状态
			status = intent.getIntExtra("status", -1);
			switch (status) {
			case MusicService.STATUS_PLAYING:
				now = intent.getIntExtra("now", 0);
				total = intent.getIntExtra("total", 0);
				seekHandle.removeMessages(PROGESS_INCREASE);
				seekHandle.sendEmptyMessageDelayed(PROGESS_INCREASE, 1000);
				seekbar.setMax(total);
				seekbar.setProgress(now);
				time_total.setText(formattime(total));
				play.setImageResource(R.drawable.pause);
				// 设置Activity的标题栏文字，提示正在播放的歌曲
				setTitleWhenPlaying();
				break;
			case MusicService.STATUS_PAUSE:
				seekHandle.sendEmptyMessage(PROGESS_PAUSE);
				play.setImageResource(R.drawable.play);
				break;
			case MusicService.STATUS_STOP:
				seekHandle.sendEmptyMessage(PROGESS_RESET);
				Main.this.setTitle("MusicPlayer");
				play.setImageResource(R.drawable.play);
				break;
			case MusicService.STATUS_COMPLETED:
				sendcommand(MusicService.COMMAND_NEXT);
				seekHandle.sendEmptyMessage(PROGESS_RESET);
				Main.this.setTitle("MusicPlayer");
				play.setImageResource(R.drawable.play);
				break;
			default:
				break;
			}
		}}
	private void bindStatuteChangedReceiver()
    {
    	receiver=new StatuteChangedReceiver();
    	IntentFilter filter=new IntentFilter(MusicService.BROADCAST_MUSICSERVICE_UPDATE_STATUS);
		registerReceiver(receiver,filter);
    }
    

	/** 设置Activity的标题栏文字，提示正在播放的歌曲 */
	private void setTitleWhenPlaying() {
		// 获取正在播放的歌曲标题
		Cursor cursor = Main.this.getMusicCursor();
		cursor.moveToPosition(number - 1);
		String title = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
		Main.this.setTitle("正在播放：" + title + " - MusicPlayer");
	}
	private Cursor getMusicCursor() {
		// 获取数据选择器
		ContentResolver resolver = getContentResolver();
		// 选择音乐媒体集
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				null);
		return cursor;
	}
	
	private boolean isplaying()
	{
		return status==MusicService.STATUS_PLAYING;
	}
	private boolean ispause()
	{
		return status==MusicService.STATUS_PAUSE;
	}
	private boolean isstop()
	{
		return status==MusicService.STATUS_STOP;
	}
	protected void onDistory()
	{
		if(isstop())
				{
			stopService(new Intent(this,MusicService.class));
				}
	super.onDestroy();
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        
        findview();
        registerlistener();
        number=1;
        status=MusicService.STATUS_STOP;
        startService(new Intent(Main.this,com.example.musicplayer.MusicService.class));
        bindStatuteChangedReceiver();
        
        sendcommand(MusicService.COMMAND_ISPLAYING);
        now=0;
        total=0;
        initseekbar();
        
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(0,1,1,"退出");
		return super.onCreateOptionsMenu(menu);
    	
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if(item.getItemId()==1)
    	{
    		this.finish();
    		
    	}
		return super.onOptionsItemSelected(item);
    	
    }
    
    private void findview()
    {
    	next=(ImageButton) findViewById(R.id.imageButton4);
    	play=(ImageButton) findViewById(R.id.imageButton2);
    	previous=(ImageButton) findViewById(R.id.imageButton1);
    	stop=(ImageButton) findViewById(R.id.imageButton3);
    	list1=(ListView) findViewById(R.id.listView1);
    	time_now=(TextView) findViewById(R.id.textView1);
    	time_total=(TextView) findViewById(R.id.textView2);
    	seekbar=(SeekBar) findViewById(R.id.seekbar1);
    }
    
    private void registerlistener()
    {
    	next.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sendcommand(MusicService.COMMAND_NEXT);	
				
			}});
    	play.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isplaying())
				{
					sendcommand(MusicService.COMMAND_PAUSE);
					
				}
				else if(ispause())
				{
					sendcommand(MusicService.COMMAND_RESUME);
				}
				else if(isstop())
				{
					sendcommand(MusicService.COMMAND_PLAY);
				}
				
			}});
    	previous.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sendcommand(MusicService.COMMAND_PREVIOUS);			
			}});
    	stop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				sendcommand(MusicService.COMMAND_STOP);	
				
			}});
    	list1.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				number=position+1;
				sendcommand(MusicService.COMMAND_PLAY);
				
				
			}});
    	seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar,
					int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				now=progress;
				time_now.setText(formattime(now));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				seekHandle.sendEmptyMessage(PROGESS_PAUSE);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				sendcommand(MusicService.COMMAND_SEEK_TO);
				if(isplaying())
				{
					seekHandle.sendEmptyMessageDelayed(PROGESS_INCREASE, 1000);
				}
			}});
    	
    }
    private void moveNumbertoPrevious()
    {
    	if(number==1)
    	{
    		number=list1.getCount();
    		Toast.makeText(Main.this, Main.this.getString(R.string.tip_reach_top),Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
    		--number;
    	}
    	
    }
    private void moveNumbertoNext()
    {
    	if((number+1)>list1.getCount())
    	{
    		number=1;
    		Toast.makeText(Main.this, Main.this.getString(R.string.tip_reach_bottom),Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
    		++number;
    	}
    }
    
    protected void onResume()
    {
    	super.onResume();
    	initmusiclist();
    	if(list1.getCount()==0)
    	{
    		next.setEnabled(false);
    		play.setEnabled(false);
    		previous.setEnabled(false);
    		stop.setEnabled(false);
    	}
    	else
    	{
    		next.setEnabled(true);
    		play.setEnabled(true);
    		previous.setEnabled(true);
    		stop.setEnabled(true);
    	}
    }
    private void initmusiclist()
    {
    	Cursor cursor=getmusiclist();
    	setlist(cursor);
    }
    private void setlist(Cursor c)
    {
    	CursorAdapter adapter=new SimpleCursorAdapter(this,android.R.layout.simple_expandable_list_item_2,c,
    			new String [] {MediaStore.Audio.AudioColumns.TITLE,
    							MediaStore.Audio.AudioColumns.ARTIST},
    							new int [] {android.R.id.text1,android.R.id.text2});
    	list1.setAdapter(adapter);
    }
    private Cursor getmusiclist()
    {
    	ContentResolver reslover=getContentResolver();
    	Cursor cursor=reslover.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null, null,null);
    	return cursor;
    }

    private void sendcommand(int command)
    {
    	Intent intent=new Intent(MusicService.BROADCAST_MUSICSERVICE_CONTROL);
    	intent.putExtra("command", command);
    	switch(command){
    	case MusicService.COMMAND_PLAY:
    		intent.putExtra("number", number);
    		break;
    	case MusicService.COMMAND_PREVIOUS:
    		moveNumbertoPrevious();
    		intent.putExtra("number", number);
    		break;
    	case MusicService.COMMAND_NEXT:
    		moveNumbertoNext();
    		intent.putExtra("number", number);
    		break;
    	case MusicService.COMMAND_SEEK_TO:
    		intent.putExtra("now", now);
    		break;
    	case MusicService.COMMAND_PAUSE:
    	case MusicService.COMMAND_RESUME:
    	case MusicService.COMMAND_STOP:
    	default:
    	break;
    	}
    	sendBroadcast(intent);
    }
    private String formattime(int msec)
    {
    	int min=(msec/1000)/60;
    	int sec=(msec/1000)%60;
    	String minstr;
    	String secstr;
    	if(min<10)
    	{
    		minstr="0"+min;
    	}
    	else
    	{
    		minstr=""+min;
    	}
    	if(sec<10)
    	{
    		secstr="0"+sec;
    	}
    	else
    	{
    		secstr=""+sec;
    	}
    	return minstr+":"+secstr;
    }
    private void initseekbar() {
    	seekHandle = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				switch (msg.what) {
				case PROGESS_INCREASE:
					if (seekbar.getProgress() < total) {
						// 进度条前进1秒
						seekbar.incrementProgressBy(1000);
						seekHandle.sendEmptyMessageDelayed(
								PROGESS_INCREASE, 1000);
						// 修改显示当前进度的文本
						time_now.setText(formattime(now));
						now += 1000;
					}
					break;
				case PROGESS_PAUSE:
					seekHandle.removeMessages(PROGESS_INCREASE);
					break;
				case PROGESS_RESET:
					// 重置进度条界面
					seekHandle.removeMessages(PROGESS_INCREASE);
					seekbar.setProgress(0);
					time_now.setText("00:00");
					break;
				}
			}
		};
	}
    

  

}
