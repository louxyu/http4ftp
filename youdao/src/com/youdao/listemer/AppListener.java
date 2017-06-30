package com.youdao.listemer;

import com.youdao.servlet.OpenApiServlet;
import com.youdao.tasks.BaseTask;
import com.youdao.tasks.TaskManage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by YW on 2017/6/9.
 */
public class AppListener implements ServletContextListener {
    private Logger logger=null;
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String log4jConfigPath = servletContextEvent.getServletContext().getInitParameter("log4jConfigPath");
        String taskClassNames = servletContextEvent.getServletContext().getInitParameter("taskClassNames");

        if(log4jConfigPath!=null && !log4jConfigPath.isEmpty()){
            InputStream in=null;
            try {
                Properties properties=new Properties();
                in=OpenApiServlet.class.getClassLoader().getResourceAsStream(log4jConfigPath);
                properties.load(in);
                PropertyConfigurator.configure(properties);
                logger = LogManager.getLogger(AppListener.class);
            } catch (IOException e) {
                logger.error(e);
            }finally {
                if(in!=null) try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }

        if (taskClassNames!=null && !taskClassNames.isEmpty()) {
            String[] classNames = taskClassNames.split(",");
            for (int i = 0; i < classNames.length; i++) {
                try {
                    BaseTask baseTask = (BaseTask) Class.forName(classNames[i]).newInstance();
                    TaskManage.addTask(baseTask,0,60);
                    logger.info("添加任务："+baseTask);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

        logger.info("----------------------初始化log4j及定时任务成功！");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
       TaskManage.shutdown();
        logger.info("----------------------定时任务停止成功！");
    }
}
