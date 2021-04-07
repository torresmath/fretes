package br.com.zup.torresmath.server

import io.micronaut.grpc.server.GrpcEmbeddedServer
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller
class GrpcServerController(@Inject val grpcServer: GrpcEmbeddedServer) {

    @Get("/grpc-server/stop")
    fun stop(): HttpResponse<String> {

        grpcServer.stop()


        return ok("Is running? ${grpcServer.isRunning}")
    }
}