package com.world.myapplication;


import java.io.Serializable;
import java.util.ArrayList;
class Request implements Serializable{
    private static long serialVersionUID  = 1L;
    class Methods{
        public static final int PULL = 0;
        public static final int NOTIFY = 1;
        public static final int STATUS = 2;
        public static final int SEARCH = 3;
        public static final int PUSH = 4;
        public static final int NEXT_CHUNK = 5;
        public static final int THE_END = 6;
    }
    class StatusCodes {
        public static final int MALFORMED_REQUEST = 400;
        public static final int NOT_FOUND = 404;

        public static final int OK = 200;

        public static final int NOT_RESPONSIBLE = 300;
    }
    static class RequestToPublisher implements Serializable{
        private static long serialVersionUID  = 2L;
        int method; //can be null

        //Fields for the pull method
        String artistName;
        String songName;

        //Fields for the search method
        String query;

        @Override
        public String toString() {
            return "RequestToPublisher{" +
                    "method=" + method +
                    ", artistName='" + artistName + '\'' +
                    ", songName='" + songName + '\'' +
                    ", query='" + query + '\'' +
                    '}';
        }
    }
    static class ReplyFromPublisher implements Serializable{
        private static long serialVersionUID  = 3L;
        int numChunks;
        int statusCode;

        //Fields for the pull method
        String artistName;
        String songName;

        //Fields for the search method
        ArrayList<MusicFileMetaData> metaData;

        @Override
        public String toString() {
            return "ReplyFromPublisher{" +
                    "numChunks=" + numChunks +
                    ", statusCode=" + statusCode +
                    ", artistName='" + artistName + '\'' +
                    ", songName='" + songName + '\'' +
                    ", metaData=" + metaData +
                    '}';
        }
    }
    static class RequestToBroker implements Serializable {
        private static long serialVersionUID  = 4L;
        int method; //Request.Methods.PULL L or Request.Methods.NOTIFY
        // or Request.Methods.STATUS  or Request.Methods.SEARCH

        //Fields for the notify method
        ArrayList<String> artistNames = new ArrayList<>();
        String publisherIp;
        int publisherPort;

        //Fields for the pull method
        String pullArtistName;
        String songName;

        //Fields for the status method
        //None

        //Fields for the search method
        String query;

        @Override
        public String toString() {
            return "RequestToBroker{" +
                    "method=" + method +
                    ", artistNames=" + artistNames +
                    ", publisherIp='" + publisherIp + '\'' +
                    ", publisherPort=" + publisherPort +
                    ", pullArtistName='" + pullArtistName + '\'' +
                    ", songName='" + songName + '\'' +
                    ", query='" + query + '\'' +
                    '}';
        }
    }

    static class ReplyFromBroker implements Serializable {
        private static long serialVersionUID  = 5L;
        int method; //METHOD_PULL or METHOD_NOTIFY or METHOD_STATUS  or (METHOD_SEARCH)
        int statusCode;
        //Fields for the notify method
        //Nonde

        //Fields for the pull method
        int numChunks;
        String responsibleBrokerIp;
        int responsibleBrokerPort;

        //Fields for the status method
        ArrayList<String> artists;

        //Fields for the search method
        ArrayList<MusicFileMetaData> metaData;

        @Override
        public String toString() {
            return "ReplyFromBroker{" +
                    "method=" + method +
                    ", statusCode=" + statusCode +
                    ", numChunks=" + numChunks +
                    ", responsibleBrokerIp='" + responsibleBrokerIp + '\'' +
                    ", responsibleBrokerPort=" + responsibleBrokerPort +
                    ", artists=" + artists +
                    ", metaData=" + metaData +
                    '}';
        }
    }
}

