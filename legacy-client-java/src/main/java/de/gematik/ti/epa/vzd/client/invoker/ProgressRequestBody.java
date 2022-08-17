/*
 * I_Directory_Administration
 * REST Schnittstelle zur Pflege der Verzeichniseinträge. Über diese Schnittstelle können Verzeichniseinträge inklusive Zertifikaten erzeugt, aktualisiert und gelöscht werden. Die Administration von Fachdaten erfolgt über Schnittstelle I_Directory_Application_Maintenance und wird durch die Fachanwendungen durchgeführt. Lesender Zugriff auf die Fachdaten ist mit Operation getDirectoryEntries in vorliegender Schnittstelle möglich.
 *
 * The version of the OpenAPI document: 1.6.3
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package de.gematik.ti.epa.vzd.client.invoker;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.*;

import java.io.IOException;

public class ProgressRequestBody extends RequestBody {

  private final RequestBody requestBody;

  private final ApiCallback callback;

  public ProgressRequestBody(RequestBody requestBody, ApiCallback callback) {
    this.requestBody = requestBody;
    this.callback = callback;
  }

  @Override
  public MediaType contentType() {
    return requestBody.contentType();
  }

  @Override
  public long contentLength() throws IOException {
    return requestBody.contentLength();
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    BufferedSink bufferedSink = Okio.buffer(sink(sink));
    requestBody.writeTo(bufferedSink);
    bufferedSink.flush();
  }

  private Sink sink(Sink sink) {
    return new ForwardingSink(sink) {

      long bytesWritten = 0L;
      long contentLength = 0L;

      @Override
      public void write(Buffer source, long byteCount) throws IOException {
        super.write(source, byteCount);
        if (contentLength == 0) {
          contentLength = contentLength();
        }

        bytesWritten += byteCount;
        callback.onUploadProgress(bytesWritten, contentLength, bytesWritten == contentLength);
      }
    };
  }
}
