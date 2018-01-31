package com.github.imageviewer.helper;

/**
 * Created by AT-Praveen on 30/01/18.
 */

public class FlickrImage {
    private String flickrPhotoURI;
    private String title;

    //Bitmap FlickrBitmap;

    public FlickrImage(String _FlickrPhotoURI, String _Title){
        flickrPhotoURI = _FlickrPhotoURI;
        title = _Title;
    }

    public String getFlickrPhotoURI() {
        return flickrPhotoURI;
    }

    public void setFlickrPhotoURI(String flickrPhotoURI) {
        this.flickrPhotoURI = flickrPhotoURI;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



        /*private Bitmap preloadBitmap(){
            Bitmap bm= null;

            String FlickrPhotoPath =
                    "http://farm" + Farm + ".static.flickr.com/"
                            + Server + "/" + Id + "_" + Secret + "_m.jpg";

            URL FlickrPhotoUrl = null;

            try {
                FlickrPhotoUrl = new URL(FlickrPhotoPath);

                HttpURLConnection httpConnection
                        = (HttpURLConnection) FlickrPhotoUrl.openConnection();
                httpConnection.setDoInput(true);
                httpConnection.connect();
                InputStream inputStream = httpConnection.getInputStream();
                bm = BitmapFactory.decodeStream(inputStream);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return bm;
        }

        public Bitmap getBitmap(){
            return FlickrBitmap;
        }*/

}
