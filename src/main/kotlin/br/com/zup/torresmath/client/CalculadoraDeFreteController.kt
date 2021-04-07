package br.com.zup.torresmath.client

import br.com.zup.torresmath.CalculaFreteRequest
import br.com.zup.torresmath.ErrorDetails
import br.com.zup.torresmath.FretesServiceGrpc
import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.exceptions.HttpStatusException
import javax.inject.Inject

@Controller
class CalculadoraDeFreteController(@Inject val gRpcClient: FretesServiceGrpc.FretesServiceBlockingStub) {

    @Get("/api/fretes")
    fun calcula(@QueryValue cep: String): FreteResponse {
        val request = CalculaFreteRequest.newBuilder()
            .setCep(cep)
            .build()

        println("REST Request: $request")

        try {
            val response = gRpcClient.calculaFrete(request)
            return FreteResponse(response.cep, response.valor)
        } catch (e: StatusRuntimeException) {

            val description = e.status.description
            val statusCode = e.status.code

            if (statusCode == Status.Code.INVALID_ARGUMENT) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, description)
            }

            if (statusCode == Status.Code.PERMISSION_DENIED) {
                val statusProto: com.google.rpc.Status? = StatusProto.fromThrowable(e)

                if (statusProto == null) {
                    throw HttpStatusException(HttpStatus.FORBIDDEN, description)
                }

                val details: Any = statusProto.detailsList.get(0)
                val errorDetails = details.unpack(ErrorDetails::class.java)

                throw HttpStatusException(HttpStatus.FORBIDDEN, "${errorDetails.code}: ${errorDetails.message}")
            }

            throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}

data class FreteResponse(val cep: String, val valor: Double)
