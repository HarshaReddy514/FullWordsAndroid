package helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpConnection {
    public static String getHttpResponse(String pURL, String pHttpMethod, String pRequestBody, HashMap< String, String > pHeaderValue) {
        try {
            String TAG = "HttpConnection";
            HttpURLConnection lHttpURLConnection = (HttpURLConnection) new URL(pURL).openConnection();
            lHttpURLConnection.setRequestMethod(pHttpMethod);
            for (Map.Entry< String, String > lMapEntrySet: pHeaderValue.entrySet()) {
                lHttpURLConnection.addRequestProperty((String) lMapEntrySet.getKey(), (String) lMapEntrySet.getValue());
            }
            lHttpURLConnection.setDoInput(true);
            if (pHttpMethod.equals("POST")) {
                lHttpURLConnection.setDoOutput(true);
                OutputStreamWriter lOutPutStreamWriter = new OutputStreamWriter(lHttpURLConnection.getOutputStream());
                lOutPutStreamWriter.write(pRequestBody);
                lOutPutStreamWriter.flush();
                lOutPutStreamWriter.close();
            }
            lHttpURLConnection.connect();
            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lHttpURLConnection.getInputStream()));
            StringBuilder lResponseStringSB = new StringBuilder();
            while (true) {
                String lResponseData = lBufferedReader.readLine();
                if (lResponseData != null) {
                    lResponseStringSB.append(lResponseData);
                } else {
                    lHttpURLConnection.disconnect();
                    return lResponseStringSB.toString();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        } catch (Exception e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
