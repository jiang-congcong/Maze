package com.example.administrator.mazeapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.Stack;
import java.util.Vector;


public class Maze extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Bitmap bitmap1;
    private boolean mIsDrawing;
    //路径
    private Path path;
    //矩形1 画笔
    private Paint ract1BackPaint;
    //矩形2 画笔
    private Paint ract2BackPaint;
    //生成两个类似文字框
    private  Paint buttonBackPaint;
    //搜索路径块
    private  Paint roadBackPaint;
    //搜索中已访问栈
    private Stack<Point> mstack =new Stack<Point>();
    //矩形1  width=900  height=900
    //Rect rect = new Rect(100,100,1000,1000);
    Rect rect = new Rect(30,100,770,800);


    //做一个int[10][10]的迷宫cell_width=90 cell_height=90
    int[][] maze1=new int[21][21];
    int[][] maze2=new int[10][10];

    public Maze(Context context) {
        super(context);
        set_attr();
    }

    public void set_attr()
    {
        surfaceHolder=getHolder();
        //添加回调
        surfaceHolder.addCallback(this);
        path =new Path();
        //添加画笔
        ract1BackPaint = new Paint();
        ract1BackPaint.setStyle(Paint.Style.FILL);
        ract1BackPaint.setColor(Color.BLACK);

        ract2BackPaint =new Paint();
        //nBackPaint.setStyle(Paint.Style.FILL);
        ract2BackPaint.setStyle(Paint.Style.STROKE);
        //nBackPaint.setColor(Color.RED);
        ract2BackPaint.setColor(Color.WHITE);


        buttonBackPaint =new Paint();
        buttonBackPaint.setStyle( Paint.Style.FILL);
        buttonBackPaint.setColor(Color.BLUE);
        buttonBackPaint.setTextSize(60);
        //buBackPaint.setColor(Color.YELLOW);

        roadBackPaint =new Paint();
        //seBackPaint.setStyle(Paint.Style.STROKE);
        roadBackPaint.setStyle(Paint.Style.FILL);
        //seBackPaint.setColor(Color.GREEN);
        roadBackPaint.setColor(Color.WHITE);

    }


    class Node
    {
        public int x;
        public int y;
        Node(){x=0; y=0;}
        Node(int xx,int yy){x=xx; y=yy;}
    }

    //运动方向
    Node[] walk_dir={new Node(1,0),new Node(-1,0),new Node(0,1),new Node(0,-1)};

    //0为可走，1为不可走
    void init() {  //初始化全部为0
        int i, j;
        for (i = 0; i < 21; i++)
            for (j = 0; j < 21; j++)
                maze1[i][j] = 0;

        //相当于maze[10][10]是通的
        for (i = 0; i < 10; i++)
            for (j = 0; j < 10; j++)
                maze1[i * 2 + 1][j * 2 + 1] = 1;

        for (i = 0; i < 10; i++)
            for (j = 0; j < 10; j++)
                maze2[i][j] = 0;
    }


    Node RandNode(Vector<Node> v,int size)
    {
        Random r=new Random();
        int t=r.nextInt(size);
        return v.elementAt(t);
    }

    void FindWay()
    {
        int i,j;
        int tx,ty;
        //acc为已访问，noacc为未访问,针对的是maze
        Vector<Node> acc=new Vector<Node>();
        Vector<Node> noacc=new Vector<Node>();

        //开始小maze所有元素加入未访问列表noacc
        for( i=0;i<10;i++)
            for(j=0;j<10;j++)
                noacc.addElement(new Node(i,j));

        //从noacc中随机选取一个元素
        Node temp=RandNode(noacc,noacc.size());
        noacc.removeElement(temp);
        acc.addElement(temp);
        maze2[temp.x][temp.y]=1;

        while(acc.size()<100)
        {
            //reverse表示访问过  verse表示没有访问过
            Vector<Integer> reverse=new Vector<Integer>();
            Vector<Integer> verse =new Vector<Integer>();

            for(i=0;i<4;i++)
            {
                tx=temp.x+walk_dir[i].x;
                ty=temp.y+walk_dir[i].y;
                //保证移动后的点在小maze中
                if(tx>=0&&tx<10&&ty>=0&&ty<10)
                {
                    //找未访问过的点
                    if(maze2[tx][ty]==0) //没有被访问过
                    {
                        verse.addElement(new Integer(i));
                    }
                    else //被访问过
                    {
                        reverse.addElement(new Integer(i));
                    }

                }

            }

            //如果四周都被访问过,则可能为死点，将四周打通且打通一个2*2的空间
            if(verse.size()==0)
            {
                temp=RandNode(acc,acc.size());
            }
            //存在没有被访问的点
            else
            {
                Random r=new Random();
                int t=r.nextInt(verse.size());
                int m=verse.elementAt(t);
                maze1[temp.x*2+1+walk_dir[m].x][temp.y*2+1+walk_dir[m].y]=1;
                temp=new Node(temp.x+walk_dir[m].x,temp.y+walk_dir[m].y);
                noacc.removeElement(temp);
                acc.addElement(temp);
                maze2[temp.x][temp.y]=1;

            }

        }

    }


    void Generate()
    {
        init();
        FindWay();

    }


    /**
     * 优化深度优先算法
     *
     * 1.每次获取四个方向的下一位置
     *
     * 2.比较四个方向哪一个最接近终点。
     *
     * 3.每次选择最接近终点的位置
     *
     *这样可以减少一些不必要的查找，例如：终点在下方，且下方位置有效，但因为上方向的优先高于下方，所以选择了上方向
     */

    //全部进过栈内的元素
    public Stack<Point> togetherstack =new Stack<Point>();

    class Point
    {
        int x;
        int y;
        //四个方向是否已经被访问过
        int[] expanddirection={-1,-1,-1,-1};
        //下一点的方向
        int diretion;
        Point(){x=0;y=0;}
        Point(int xx,int yy){x=xx;y=yy;}
    }

    //寻找路径
    public void Searchroad()
    {
        int i;
        int count=0;
        Point toppoint=null;
        Point bestpoint=new Point();
        Point nextpoint=null;
        //起点
        Point start=new Point(0,1);
        //终点
        Point end=new Point(19,19);
        //起点入栈
        mstack.push(start);
        togetherstack.push(start);
        while(!mstack.isEmpty()&&!(bestpoint.x==end.x&&bestpoint.y==end.y))
        {
            toppoint=mstack.peek();
            bestpoint=new Point();
            nextpoint=null;

            for(i=0;i<4;i++)
            {
                if(i==0)
                {
                    nextpoint=new Point(toppoint.x+1,toppoint.y);//右
                }
                else if(i==1)
                {
                    nextpoint=new Point(toppoint.x-1,toppoint.y); //左
                }
                else if(i==2)
                {
                    nextpoint=new Point(toppoint.x,toppoint.y+1);//上
                }
                else if(i==3)
                {
                    nextpoint=new Point(toppoint.x,toppoint.y-1);//下
                }

                if(!check_right(nextpoint))
                {
                    continue;
                }

                nextpoint.diretion=i;

                if(bestpoint.equals(new Point(0,0)))
                {
                    bestpoint=nextpoint;
                }
                else{ //计算下一个点到终点的距离
                    int bestpointpath=(bestpoint.x-end.x)*(bestpoint.x-end.x)+(bestpoint.y-end.y)*(bestpoint.y-end.y);
                    int nextpointpath=(nextpoint.x-end.x)*(nextpoint.x-end.x)+(nextpoint.y-end.y)*(nextpoint.y-end.y);
                    if(nextpointpath<bestpointpath)
                        bestpoint=nextpoint;
                }

            }

            if(bestpoint.x==0&&bestpoint.y==0)
            {
                mstack.pop();

            }
            else
            {
                mstack.push(bestpoint);
                togetherstack.push(bestpoint);

            }

        }

    }


    //判断此点是否符合要求
    //在范围内，不是墙，不在栈内过
    public boolean check_right(Point point)
    {

        if(point.x<1||point.x>20||point.y<1||point.y>20)
        {
            return false;
        }

        if(maze1[point.x][point.y]==0)
        {
            return false;
        }

        for(int i=0;i<togetherstack.size();i++)
        {
            if(point.x==togetherstack.elementAt(i).x&&point.y==togetherstack.elementAt(i).y)
            {
                return false;
            }

        }
        return true;

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing=true;
        //开始画图操作
        new Thread(new DrawThread()).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    public void drawstatic(Canvas cavans)
    {
        int i=0;
        int j=0;
        //int cell_width=43;
        //int cell_height=43;
        int cell_width=30;
        int cell_height=30;

        if(bitmap1==null) {
            bitmap1 = Bitmap.createBitmap(1000, 1800, Bitmap.Config.ARGB_8888);
            //bitmap1 = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap1);
            //初始化画布并在画布上画一些东西
            //mStaticCanvas.drawColor(Color.GREEN);
            canvas.drawColor(Color.WHITE); //color1
            canvas.drawRect(rect, ract1BackPaint);
            //画起点
            //canvas.drawRect(100 + cell_width, 100, 100 + cell_width + cell_width, 100 + cell_height, ract2BackPaint);
            canvas.drawRect(60 + cell_width, 100, 60 + cell_width + cell_width, 100 + cell_height, ract2BackPaint);
            //画终点
            //canvas.drawRect(100 + 19 * cell_width, 100 + 20 * cell_height, 100 + 19 * cell_width + cell_width, 100 + 20 * cell_height + cell_height, ract2BackPaint);
            canvas.drawRect(60 + 19 * cell_width, 100 + 20 * cell_height, 60 + 19 * cell_width + cell_width, 100 + 20 * cell_height + cell_height, ract2BackPaint);
            //画随机地图生成按钮
            //mStaticCanvas.drawRect(100, 1100, 400, 1200, buBackPaint);

            //canvas.drawText("生成地图", 150, 1200, buttonBackPaint);
            //canvas.drawText("生成地图", 150, 950, buttonBackPaint);
            canvas.drawText("生成地图", 100, 950, buttonBackPaint);
            //画搜索路径按钮
            //mStaticCanvas.drawRect(100, 1300, 400, 1400, mBackPaint);
            //mStaticCanvas.drawRect(600, 1100, 900, 1200, mBackPaint);
            //canvas.drawText("寻找路径", 550, 1200, buttonBackPaint);
            //canvas.drawText("寻找路径", 550, 950, buttonBackPaint);
            canvas.drawText("寻找路径", 400, 950, buttonBackPaint);

        }
        cavans.drawBitmap(bitmap1,0,0,null);
    }


    class DrawThread implements Runnable
    {
        int i=0;
        int j=0;
        //int cell_width=43;
        //int cell_height=43;

        int cell_width=30;
        int cell_height=30;
        @Override
        public void run()
        {
            Canvas canvas=null;
            synchronized (surfaceHolder)
            {
                canvas=surfaceHolder.lockCanvas();
                //canvas.drawColor(Color.GREEN);
                canvas.drawColor(Color.WHITE); //color1
                drawstatic(canvas);
                Generate();
                for(i=0;i<21;i++)
                    for(j=0;j<21;j++)
                    {
                        if(maze1[i][j]==1)
                        {
                            //canvas.drawRect(100+j*cell_width,100+i*cell_height,100+j*cell_width+cell_width,100+i*cell_height+cell_height,ract2BackPaint);
                            canvas.drawRect(60+j*cell_width,100+i*cell_height,60+j*cell_width+cell_width,100+i*cell_height+cell_height,ract2BackPaint);
                        }
                    }
                // canvas.drawBitmap(mStaticBitmap,0,0,null);
            }
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

        }
    }



    class DrawPathThread extends Thread
    {
        int i,j;
        //int cell_width=43;
        //int cell_height=43;

        int cell_width=30;
        int cell_height=30;
        @Override
        public void run()
        {
            Canvas canvas=null;
            synchronized (surfaceHolder)
            {
                canvas=surfaceHolder.lockCanvas();
                //canvas.drawColor(Color.GREEN);
                canvas.drawColor(Color.WHITE);//color1

                drawstatic(canvas);
                //将之前的地图画出来
                for(i=0;i<21;i++)
                    for(j=0;j<21;j++)
                    {
                        if(maze1[i][j]==1)
                        {
                            //canvas.drawRect(100+j*cell_width,100+i*cell_height,100+j*cell_width+cell_width,100+i*cell_height+cell_height,ract2BackPaint);
                            canvas.drawRect(60+j*cell_width,100+i*cell_height,60+j*cell_width+cell_width,100+i*cell_height+cell_height,ract2BackPaint);

                        }
                    }

                //将路径给画出来
                Searchroad();
                while(!mstack.isEmpty())
                {
                    Point top=mstack.peek();
                    int tx=top.x;
                    int ty=top.y;
                    //canvas.drawRect(100+ty*cell_width,100+tx*cell_height,100+ty*cell_width+cell_width,100+tx*cell_height+cell_height,roadBackPaint);
                    canvas.drawRect(60+ty*cell_width,100+tx*cell_height,60+ty*cell_width+cell_width,100+tx*cell_height+cell_height,roadBackPaint);
                    mstack.pop();
                }
                //canvas.drawRect(100+19*cell_width,100+20*cell_height,100+19*cell_width+cell_width,100+20*cell_height+cell_height,roadBackPaint);
                canvas.drawRect(60+19*cell_width,100+20*cell_height,60+19*cell_width+cell_width,100+20*cell_height+cell_height,roadBackPaint);
                togetherstack.clear();
            }
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

        }
    }




    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            float xx=event.getX();
            float yy=event.getY();

            if(xx>100&&xx<300&&yy>850&&yy<950)
            {
                new Thread(new DrawThread()).start();
                return true;
            }

            else if(xx>400&&xx<600&&yy>850&&yy<950)
            {
                new Thread(new DrawPathThread()).start();
                return true;
            }

            else
                return false;
        }

        return false;
    }


}
