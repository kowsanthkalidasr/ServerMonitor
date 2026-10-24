package com.sermon.node_metrics.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class NodeTelemetryServiceGrpc {

  private NodeTelemetryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.sermon.node.grpc.NodeTelemetryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.NodeStatusRequest,
      com.sermon.node_metrics.grpc.NodeStatusMetrics> getStreamNodeMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamNodeMetrics",
      requestType = com.sermon.node_metrics.grpc.NodeStatusRequest.class,
      responseType = com.sermon.node_metrics.grpc.NodeStatusMetrics.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.NodeStatusRequest,
      com.sermon.node_metrics.grpc.NodeStatusMetrics> getStreamNodeMetricsMethod() {
    io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.NodeStatusRequest, com.sermon.node_metrics.grpc.NodeStatusMetrics> getStreamNodeMetricsMethod;
    if ((getStreamNodeMetricsMethod = NodeTelemetryServiceGrpc.getStreamNodeMetricsMethod) == null) {
      synchronized (NodeTelemetryServiceGrpc.class) {
        if ((getStreamNodeMetricsMethod = NodeTelemetryServiceGrpc.getStreamNodeMetricsMethod) == null) {
          NodeTelemetryServiceGrpc.getStreamNodeMetricsMethod = getStreamNodeMetricsMethod =
              io.grpc.MethodDescriptor.<com.sermon.node_metrics.grpc.NodeStatusRequest, com.sermon.node_metrics.grpc.NodeStatusMetrics>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamNodeMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sermon.node_metrics.grpc.NodeStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sermon.node_metrics.grpc.NodeStatusMetrics.getDefaultInstance()))
              .setSchemaDescriptor(new NodeTelemetryServiceMethodDescriptorSupplier("StreamNodeMetrics"))
              .build();
        }
      }
    }
    return getStreamNodeMetricsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NodeTelemetryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceStub>() {
        @java.lang.Override
        public NodeTelemetryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeTelemetryServiceStub(channel, callOptions);
        }
      };
    return NodeTelemetryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static NodeTelemetryServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceBlockingV2Stub>() {
        @java.lang.Override
        public NodeTelemetryServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeTelemetryServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return NodeTelemetryServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NodeTelemetryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceBlockingStub>() {
        @java.lang.Override
        public NodeTelemetryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeTelemetryServiceBlockingStub(channel, callOptions);
        }
      };
    return NodeTelemetryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NodeTelemetryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeTelemetryServiceFutureStub>() {
        @java.lang.Override
        public NodeTelemetryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeTelemetryServiceFutureStub(channel, callOptions);
        }
      };
    return NodeTelemetryServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void streamNodeMetrics(com.sermon.node_metrics.grpc.NodeStatusRequest request,
        io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeStatusMetrics> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStreamNodeMetricsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service NodeTelemetryService.
   */
  public static abstract class NodeTelemetryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return NodeTelemetryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service NodeTelemetryService.
   */
  public static final class NodeTelemetryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<NodeTelemetryServiceStub> {
    private NodeTelemetryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeTelemetryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeTelemetryServiceStub(channel, callOptions);
    }

    /**
     */
    public void streamNodeMetrics(com.sermon.node_metrics.grpc.NodeStatusRequest request,
        io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeStatusMetrics> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getStreamNodeMetricsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service NodeTelemetryService.
   */
  public static final class NodeTelemetryServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<NodeTelemetryServiceBlockingV2Stub> {
    private NodeTelemetryServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeTelemetryServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeTelemetryServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, com.sermon.node_metrics.grpc.NodeStatusMetrics>
        streamNodeMetrics(com.sermon.node_metrics.grpc.NodeStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getStreamNodeMetricsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service NodeTelemetryService.
   */
  public static final class NodeTelemetryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<NodeTelemetryServiceBlockingStub> {
    private NodeTelemetryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeTelemetryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeTelemetryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<com.sermon.node_metrics.grpc.NodeStatusMetrics> streamNodeMetrics(
        com.sermon.node_metrics.grpc.NodeStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getStreamNodeMetricsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service NodeTelemetryService.
   */
  public static final class NodeTelemetryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<NodeTelemetryServiceFutureStub> {
    private NodeTelemetryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeTelemetryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeTelemetryServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_STREAM_NODE_METRICS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAM_NODE_METRICS:
          serviceImpl.streamNodeMetrics((com.sermon.node_metrics.grpc.NodeStatusRequest) request,
              (io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeStatusMetrics>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getStreamNodeMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.sermon.node_metrics.grpc.NodeStatusRequest,
              com.sermon.node_metrics.grpc.NodeStatusMetrics>(
                service, METHODID_STREAM_NODE_METRICS)))
        .build();
  }

  private static abstract class NodeTelemetryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NodeTelemetryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.sermon.node_metrics.grpc.NodeMetricsProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NodeTelemetryService");
    }
  }

  private static final class NodeTelemetryServiceFileDescriptorSupplier
      extends NodeTelemetryServiceBaseDescriptorSupplier {
    NodeTelemetryServiceFileDescriptorSupplier() {}
  }

  private static final class NodeTelemetryServiceMethodDescriptorSupplier
      extends NodeTelemetryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    NodeTelemetryServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (NodeTelemetryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NodeTelemetryServiceFileDescriptorSupplier())
              .addMethod(getStreamNodeMetricsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
