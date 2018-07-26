package com.universalvideoviewsample;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    public interface ResponseCallback {
        void onFailure(Exception e);

        void onSuccess(int responseCode, String response);

        void onSuccess(String contentType, InputStream inputStream);
    }

    static class HttpResponse {
        private int responseCode;
        private String response;
        private Exception exception;
        private InputStream inputStream;
        private String contentType;

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    public static void get(String path, final ResponseCallback responseCallback) {
        new AsyncTask<String, Void, HttpResponse>() {
            @Override
            protected HttpResponse doInBackground(String... params) {
                HttpResponse httpResponse = new HttpResponse();
                HttpURLConnection httpURLConnection = null;
                try {
                    URL url = new URL(params[0]);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    httpResponse.setResponseCode(httpURLConnection.getResponseCode());
                    httpResponse.setResponse(response.toString());
                } catch (IOException e) {
                    httpResponse.setException(e);
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
                return httpResponse;
            }

            @Override
            protected void onPostExecute(HttpResponse httpResponse) {
                super.onPostExecute(httpResponse);
                if (responseCallback != null) {
                    if (httpResponse.getResponseCode() == 0) {
                        responseCallback.onFailure(httpResponse.getException());
                    } else {
                        responseCallback.onSuccess(httpResponse.getResponseCode(), httpResponse.getResponse());
                    }
                }
            }
        }.execute(path);
    }

    public static void proxy(String path, final ResponseCallback responseCallback) {
        new AsyncTask<String, Void, HttpResponse>() {
            @Override
            protected HttpResponse doInBackground(String... params) {
                HttpResponse httpResponse = new HttpResponse();
                HttpURLConnection httpURLConnection;
                try {
                    URL url = new URL(params[0]);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    InputStream inputStream = httpURLConnection.getInputStream();
                    httpResponse.setResponseCode(httpURLConnection.getResponseCode());
                    httpResponse.setInputStream(inputStream);
                    String contentType = httpURLConnection.getContentType();
                    if (contentType.contains(";")) {
                        contentType = contentType.substring(0, contentType.indexOf(";"));
                    }
                    httpResponse.setContentType(contentType);
                } catch (IOException e) {
                    httpResponse.setException(e);
                }
                return httpResponse;
            }

            @Override
            protected void onPostExecute(HttpResponse httpResponse) {
                super.onPostExecute(httpResponse);
                if (responseCallback != null) {
                    if (httpResponse.getResponseCode() == 0) {
                        responseCallback.onFailure(httpResponse.getException());
                    } else {
                        responseCallback.onSuccess(httpResponse.getContentType(), httpResponse.getInputStream());
                    }
                }
            }
        }.execute(path);
    }
}

