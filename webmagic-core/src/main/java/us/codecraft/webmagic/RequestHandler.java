package us.codecraft.webmagic;



public class RequestHandler {



        public void processRequest(Request request, Spider spider) {
            Page page;
            if (null != request.getDownloader()){
                page = request.getDownloader().download(request, spider);
            } else {
                page = spider.getDownloader().download(request, spider);
            }

            if (page.isDownloadSuccess()) {
                onDownloadSuccess(request, page, spider);
            } else {
                onDownloaderFail(request, spider);
            }
        }

        private void onDownloadSuccess(Request request, Page page, Spider spider) {
            if (spider.getSite().getAcceptStatCode().contains(page.getStatusCode())) {

                spider.getPageProcessor().process(page);
                spider.extractAndAddRequests(page, spider.isSpawnUrl());

                if (!page.getResultItems().isSkip()) {
                    for (Pipeline pipeline : spider.getPipelines()) {
                        pipeline.process(page.getResultItems(), spider);
                    }
                }

            } else {
                spider.getLogger().info("page status code error, page {} , code: {}", request.getUrl(), page.getStatusCode());
            }

            spider.sleep(spider.getSite().getSleepTime());
        }

        private void onDownloaderFail(Request request, Spider spider) {
            if (spider.getSite().getCycleRetryTimes() == 0) {
                spider.sleep(spider.getSite().getSleepTime());
            } else {
                doCycleRetry(request, spider);
            }
        }

        private void doCycleRetry(Request request, Spider spider) {
            Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);

            if (cycleTriedTimesObject == null) {
                spider.addRequest(
                        request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, 1)
                );
            } else {
                int cycleTriedTimes = (Integer) cycleTriedTimesObject;
                cycleTriedTimes++;

                if (cycleTriedTimes < spider.getSite().getCycleRetryTimes()) {
                    spider.addRequest(
                            request.setPriority(0).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes)
                    );
                }
            }

            spider.sleep(spider.getSite().getRetrySleepTime());
        }

}
