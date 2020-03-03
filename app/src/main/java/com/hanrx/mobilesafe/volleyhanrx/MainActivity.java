package com.hanrx.mobilesafe.volleyhanrx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hanrx.mobilesafe.volleyhanrx.db.BaseDaoFactory;
import com.hanrx.mobilesafe.volleyhanrx.http.download.DownFileManager;
import com.hanrx.mobilesafe.volleyhanrx.update.UpdateManager;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //public static final String url = "http://192.168.100.8080/UserRecord/LoginServlet";
    public static final String url = "http://v.juhe.cn/toutiao/index?type=top&key=6ccd6aae7830a31618bc1aab1e41be33";
    private static final String TAG = "hanrx";
    TextView textView;
    UpdateManager updateManager;
    UserDao baseDao;
    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView= (TextView) findViewById(R.id.content);
        updateManager=new UpdateManager();
        baseDao= BaseDaoFactory.getInstance().getDataHelper(UserDao.class,User.class);
    }

    /**
     *  1
     *  2
     * @param view
     */
    public  void login(View view)
    {

        User user=new User();
        user.setName("V00"+(i++));
        user.setPassword("123456");
        user.setName("张三"+i);
        user.setUser_id("N000"+i);
        baseDao.insert(user);
        updateManager.checkThisVersionTable(this);
        /*User user=new User();
        user.setName("13343491234");
        user.setPassword("123456");
        for (int i=0;i<50;i++)
        {
            Volley.sendRequest(user, url, LoginRespense.class, new IDataListener<LoginRespense>() {
                @Override
                public void onSuccess(LoginRespense loginRespense) {
                    Log.i(TAG,loginRespense.toString());
                }

                @Override
                public void onFail() {
                    Log.i(TAG,"获取失败");
                }
            });
        }*/
        /*Volley.sendRequest(null, url, NewsPager.class, new IDataListener<NewsPager>() {
            @Override
            public void onSuccess(NewsPager newsPager) {
                Log.i(TAG,newsPager.toString());
            }

            @Override
            public void onFail() {
                Log.i(TAG,"获取失败");
            }
        });*/

        /*DownFileManager downFileManager = new DownFileManager();
        downFileManager.reallyDown("http://gdown.baidu.com/data/wisegame/8be18d2c0dc8a9c9/WPSOffice_177.apk");*/
    }

    public void insert(View view)
    {
        Photo photo=new Photo();
        photo.setPath("data/data/my.jpg");
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        photo.setTime(dateFormat.format(new Date()));
        PhotoDao photoDao=BaseDaoFactory.getInstance().getUserHelper(PhotoDao.class,Photo.class);
        photoDao.insert(photo);
    }
    public void write(View view)
    {
        /**
         * 写入版本
         */
        updateManager.saveVersionInfo(this,"V002");

    }
    public void update(View view)
    {
        updateManager.checkThisVersionTable(this);

        updateManager.startUpdateDb(this);
    }
}
