package org.jetbrains.bsp.bazel.server;

import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest;
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse;
import io.grpc.stub.StreamObserver;

public class BepStreamObserver implements StreamObserver<PublishBuildToolEventStreamRequest> {

  private final BepServer bepServer;
  private final StreamObserver<PublishBuildToolEventStreamResponse> responseObserver;

  public BepStreamObserver(
      BepServer bepServer, StreamObserver<PublishBuildToolEventStreamResponse> responseObserver) {
    this.bepServer = bepServer;
    this.responseObserver = responseObserver;
  }

  @Override
  public void onNext(PublishBuildToolEventStreamRequest request) {
    if (request
        .getOrderedBuildEvent()
        .getEvent()
        .getBazelEvent()
        .getTypeUrl()
        .equals("type.googleapis.com/build_event_stream.BuildEvent")) {
      bepServer.handleEvent(request.getOrderedBuildEvent().getEvent());
    }

    PublishBuildToolEventStreamResponse response =
        PublishBuildToolEventStreamResponse.newBuilder()
            .setStreamId(request.getOrderedBuildEvent().getStreamId())
            .setSequenceNumber(request.getOrderedBuildEvent().getSequenceNumber())
            .build();

    responseObserver.onNext(response);
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println("Error from BEP stream: " + throwable);
  }

  @Override
  public void onCompleted() {
    responseObserver.onCompleted();
  }
}
