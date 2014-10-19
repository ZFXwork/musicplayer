package com.example.musicplayer;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AsyncPlayer;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

public class MusicService extends Service
{
	public static final int COMMAND_UNKNOW=-1;
	public static final int COMMAND_PLAY=0;
	public static final int COMMAND_PAUSE=1;
	public static final int COMMAND_STOP=2;
	public static final int COMMAND_RESUME=3;
	public static final int COMMAND_PREVIOUS=4;
	public static final int COMMAND_NEXT=5;
	public static final int COMMAND_ISPLAYING=6;
	public static final int COMMAND_SEEK_TO=7;
	
	public static final int STATUS_PLAYING=0;
	public static final int STATUS_PAUSE=1;
	public static final int STATUS_STOP=2;
	public static final int STATUS_COMPLETED=3;
	
	public static final String BROADCAST_MUSICSERVICE_CONTROL="MusicService.ACTION_CONTROL";
	public static final String BROADCAST_MUSICSERVICE_UPDATE_STATUS="MusicService.ACTION_DATE";
	
	private CommandReceiver receiver;
	private MediaPlayer player;
	
	private void load(int number)
	{
		if(player!=null)
		{
			player.release();
		}
		String path=null;
		ContentResolver reslover=getContentResolver();
    	Cursor cursor=reslover.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,null, null,null);
		cursor.move(number);
		path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)); 		       
			
		try{
		player=new MediaPlayer();
		player.setDataSource(path);

        player.prepare();

        player.start();
		}catch (IllegalArgumentException e) {

            e.printStackTrace();

         } catch (IllegalStateException e) {

            e.printStackTrace();

         } catch (IOException e) {

            e.printStackTrace();

         }
		player.setOnCompletionListener(completionlistener);
	}

	

	OnCompletionListener completionlistener=new OnCompletionListener()
	{
		public void onCompletion(MediaPlayer player)
		{
			if(player.isLooping())
			{
				replay();
			}
			else
			{
				sendcaststatuschange(MusicService.STATUS_COMPLETED);
			}
		}
	};
	 
	class CommandReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int command=intent.getIntExtra("command",COMMAND_UNKNOW );
			switch (command){
			case COMMAND_PLAY:
			case COMMAND_PREVIOUS:
			case COMMAND_NEXT:
				int number=intent.getIntExtra("number",1);
				Toast.makeText(MusicService.this,"正在播放第 "+number+" 首", Toast.LENGTH_SHORT).show();
				play(number);
				break;
			case COMMAND_PAUSE:
				pause();
				break;
			case COMMAND_STOP:
				stop();
				break;
			case COMMAND_RESUME:
				resume();
				break;
			case COMMAND_ISPLAYING:
			if(player.isPlaying())
			{
				sendcaststatuschange(MusicService.STATUS_PLAYING);
			}break;	
			case COMMAND_SEEK_TO:
				seekto(intent.getIntExtra("now", 0));
				sendcaststatuschange(MusicService.STATUS_PLAYING);
				break;
			case COMMAND_UNKNOW:
			default:
				break;
			}
		}
		
	}
	private void bindCommandReceiver()
	{
		receiver=new CommandReceiver();
		IntentFilter filter=new IntentFilter(BROADCAST_MUSICSERVICE_CONTROL);
		registerReceiver(receiver,filter);
	}
	private void seekto(int time)
	{
		if(player!=null)
		{
			player.seekTo(time);
		}
	}
	
	private void sendcaststatuschange(int status)
	{
		Intent intent=new Intent(BROADCAST_MUSICSERVICE_UPDATE_STATUS);
		intent.putExtra("status", status);
		if(status==STATUS_PLAYING)
		{
			intent.putExtra("now", player.getCurrentPosition());
			intent.putExtra("total", player.getDuration());
		}
		sendBroadcast(intent);
	}
	
	private void play(int number)
	{
	if(player!=null&&player.isPlaying())
		{
			player.stop();
		}
		load(number);
		//player.start();
		sendcaststatuschange(MusicService.STATUS_PLAYING);
	}
	private void replay()
	{
			player.start();
		sendcaststatuschange(MusicService.STATUS_PLAYING);
	}
	private void pause()
	{
		if(player.isPlaying())
		{
			player.pause();
			sendcaststatuschange(MusicService.STATUS_PAUSE);
		}
	}
	private void stop()
	{
		if(player!=null)
		{
			player.stop();
			sendcaststatuschange(MusicService.STATUS_STOP);
		}
	}
	private void resume()
	{
		player.start();
		sendcaststatuschange(MusicService.STATUS_PLAYING);
	}	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	  @Override
	public void onCreate()
	{
		super.onCreate();
		bindCommandReceiver();
	}
	@SuppressWarnings("deprecation")
	public void onStar(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}
	public void onDistory()
	{
		if(player!=null)
		{
			player.release();
		}
		super.onDestroy();
	}
}