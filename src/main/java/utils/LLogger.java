package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLogger {

    public static final Logger logger = LoggerFactory.getLogger("LLogger");

    public static void main(String[] args){
        System.out.println("test!!!");
        logger.info("test!!!");
        logger.debug("debug!!!");
        logger.error("error!!!");
    }
}
