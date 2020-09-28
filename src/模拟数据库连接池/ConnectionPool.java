package 模拟数据库连接池;

import java.sql.Connection;
import java.util.LinkedList;

public class ConnectionPool {
    private LinkedList<Connection> pool = new LinkedList<>();
    public ConnectionPool(int initialSize){
        for (int i = 0; i < initialSize ; i++) {
             pool.addLast(ConnectionDiver.createConnection());
        }
    }
    public void releaseConnection(Connection connection){
        if (connection != null){
            synchronized (pool){
                // 连接释放后需要进行通知,这样其他消费者能够感知连接池中已经归还了一个连接
                pool.addLast(connection);
                pool.notifyAll();
            }
        }
    }
    //在mills秒内没有获取到连接 ，将会返回null
    public Connection fetchConnection(long mills) throws InterruptedException{
        synchronized (pool){
            //完全超时
            if (mills<=0){
                while (pool.isEmpty()){
                    pool.wait();
                }
                return pool.removeFirst();
            }else {
                long future = System.currentTimeMillis() + mills;
                long remain = mills;
                while (pool.isEmpty() && remain > 0){
                    pool.wait(remain);
                    remain = future - System.currentTimeMillis();
                }
                Connection result = null;
                if (!pool.isEmpty()){
                    result = pool.removeFirst();
                }
                return result;
            }
        }
    }
}
