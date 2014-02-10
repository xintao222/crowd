

package cn.com.zhenshiyin.crowd.net;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.com.zhenshiyin.crowd.util.LogUtil;


/**
 * 线程池 、缓冲队列
 * @author zxy
 *
 */
public class DefaultThreadPool {
	/**
	 * BaseRequest任务队列
	 */
	static ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(15);
	/**
	 * 线程池
	 */
	public static AbstractExecutorService pool = new ThreadPoolExecutor(10, 20, 15L, TimeUnit.SECONDS,
																		blockingQueue, new ThreadPoolExecutor.DiscardOldestPolicy());
	
	private  static DefaultThreadPool instance = null;
	public  static DefaultThreadPool getInstance(){
		if(instance == null){
			instance = new DefaultThreadPool();
		} 
		return instance;
	}
	
	public void execute(Runnable r){

		pool.execute(r);
	}
	/**
	 * 关闭，并等待任务执行完成，不接受新任务
	 */
	public static  void shutdown(){
		if(pool!=null){
			pool.shutdown();
			LogUtil.i(DefaultThreadPool.class.getName(), "DefaultThreadPool shutdown");
		}
	}
	
	/**
	 * 关闭，立即关闭，并挂起所有正在执行的线程，不接受新任务
	 */
	public  static void shutdownRightnow(){
		if(pool!=null){
			//List<Runnable>  tasks =pool.shutdownNow();
			pool.shutdownNow();
			try {
				//设置超时极短，强制关闭所有任务
				pool.awaitTermination(1, TimeUnit.MICROSECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			LogUtil.i(DefaultThreadPool.class.getName(), "DefaultThreadPool shutdownRightnow");
//			for(Runnable   task:tasks){
//				task.
//			}
		}
	}
	
	
	public  static void removeTaskFromQueue(){
		//blockingQueue.contains(o);
	}
}
