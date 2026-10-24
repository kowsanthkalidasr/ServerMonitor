package com.sermon.node_metrics.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@io.grpc.stub.annotations.GrpcGenerated
public final class NodeRegistryServiceGrpc {

  private NodeRegistryServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.sermon.node.grpc.NodeRegistryService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.FetchNodesRequest,
      com.sermon.node_metrics.grpc.NodeIdBatch> getFetchRegisteredNodesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FetchRegisteredNodes",
      requestType = com.sermon.node_metrics.grpc.FetchNodesRequest.class,
      responseType = com.sermon.node_metrics.grpc.NodeIdBatch.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.FetchNodesRequest,
      com.sermon.node_metrics.grpc.NodeIdBatch> getFetchRegisteredNodesMethod() {
    io.grpc.MethodDescriptor<com.sermon.node_metrics.grpc.FetchNodesRequest, com.sermon.node_metrics.grpc.NodeIdBatch> getFetchRegisteredNodesMethod;
    if ((getFetchRegisteredNodesMethod = NodeRegistryServiceGrpc.getFetchRegisteredNodesMethod) == null) {
      synchronized (NodeRegistryServiceGrpc.class) {
        if ((getFetchRegisteredNodesMethod = NodeRegistryServiceGrpc.getFetchRegisteredNodesMethod) == null) {
          NodeRegistryServiceGrpc.getFetchRegisteredNodesMethod = getFetchRegisteredNodesMethod =
              io.grpc.MethodDescriptor.<com.sermon.node_metrics.grpc.FetchNodesRequest, com.sermon.node_metrics.grpc.NodeIdBatch>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FetchRegisteredNodes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sermon.node_metrics.grpc.FetchNodesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.sermon.node_metrics.grpc.NodeIdBatch.getDefaultInstance()))
              .setSchemaDescriptor(new NodeRegistryServiceMethodDescriptorSupplier("FetchRegisteredNodes"))
              .build();
        }
      }
    }
    return getFetchRegisteredNodesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static NodeRegistryServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceStub>() {
        @java.lang.Override
        public NodeRegistryServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeRegistryServiceStub(channel, callOptions);
        }
      };
    return NodeRegistryServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static NodeRegistryServiceBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceBlockingV2Stub>() {
        @java.lang.Override
        public NodeRegistryServiceBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeRegistryServiceBlockingV2Stub(channel, callOptions);
        }
      };
    return NodeRegistryServiceBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static NodeRegistryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceBlockingStub>() {
        @java.lang.Override
        public NodeRegistryServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeRegistryServiceBlockingStub(channel, callOptions);
        }
      };
    return NodeRegistryServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static NodeRegistryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<NodeRegistryServiceFutureStub>() {
        @java.lang.Override
        public NodeRegistryServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new NodeRegistryServiceFutureStub(channel, callOptions);
        }
      };
    return NodeRegistryServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void fetchRegisteredNodes(com.sermon.node_metrics.grpc.FetchNodesRequest request,
        io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeIdBatch> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchRegisteredNodesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service NodeRegistryService.
   */
  public static abstract class NodeRegistryServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return NodeRegistryServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service NodeRegistryService.
   */
  public static final class NodeRegistryServiceStub
      extends io.grpc.stub.AbstractAsyncStub<NodeRegistryServiceStub> {
    private NodeRegistryServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeRegistryServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeRegistryServiceStub(channel, callOptions);
    }

    /**
     */
    public void fetchRegisteredNodes(com.sermon.node_metrics.grpc.FetchNodesRequest request,
        io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeIdBatch> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getFetchRegisteredNodesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service NodeRegistryService.
   */
  public static final class NodeRegistryServiceBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<NodeRegistryServiceBlockingV2Stub> {
    private NodeRegistryServiceBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeRegistryServiceBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeRegistryServiceBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/10918")
    public io.grpc.stub.BlockingClientCall<?, com.sermon.node_metrics.grpc.NodeIdBatch>
        fetchRegisteredNodes(com.sermon.node_metrics.grpc.FetchNodesRequest request) {
      return io.grpc.stub.ClientCalls.blockingV2ServerStreamingCall(
          getChannel(), getFetchRegisteredNodesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service NodeRegistryService.
   */
  public static final class NodeRegistryServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<NodeRegistryServiceBlockingStub> {
    private NodeRegistryServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeRegistryServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeRegistryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<com.sermon.node_metrics.grpc.NodeIdBatch> fetchRegisteredNodes(
        com.sermon.node_metrics.grpc.FetchNodesRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getFetchRegisteredNodesMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service NodeRegistryService.
   */
  public static final class NodeRegistryServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<NodeRegistryServiceFutureStub> {
    private NodeRegistryServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected NodeRegistryServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new NodeRegistryServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_FETCH_REGISTERED_NODES = 0;

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
        case METHODID_FETCH_REGISTERED_NODES:
          serviceImpl.fetchRegisteredNodes((com.sermon.node_metrics.grpc.FetchNodesRequest) request,
              (io.grpc.stub.StreamObserver<com.sermon.node_metrics.grpc.NodeIdBatch>) responseObserver);
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
          getFetchRegisteredNodesMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              com.sermon.node_metrics.grpc.FetchNodesRequest,
              com.sermon.node_metrics.grpc.NodeIdBatch>(
                service, METHODID_FETCH_REGISTERED_NODES)))
        .build();
  }

  private static abstract class NodeRegistryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    NodeRegistryServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.sermon.node_metrics.grpc.NodeMetricsProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("NodeRegistryService");
    }
  }

  private static final class NodeRegistryServiceFileDescriptorSupplier
      extends NodeRegistryServiceBaseDescriptorSupplier {
    NodeRegistryServiceFileDescriptorSupplier() {}
  }

  private static final class NodeRegistryServiceMethodDescriptorSupplier
      extends NodeRegistryServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    NodeRegistryServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (NodeRegistryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new NodeRegistryServiceFileDescriptorSupplier())
              .addMethod(getFetchRegisteredNodesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
