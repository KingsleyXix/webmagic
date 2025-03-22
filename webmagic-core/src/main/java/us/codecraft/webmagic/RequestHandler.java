package us.codecraft.webmagic;

public class RequestHandler {



    public void processRequest(Request request, Spider spider) {
        Page page;
        if (null != request.getDownloader()){
            page = request.getDownloader().download(request, spider);
        } else {
            page = spider.getDownloader().download(request, spider);
        }

        if (page.isDownloadSuccess()){
            spider.onDownloadSuccess(request, page);
        } else {
            spider.onDownloaderFail(request);
        }
    }

}
