package br.com.zup.torresmath.server

import br.com.zup.torresmath.CalculaFreteRequest
import br.com.zup.torresmath.CalculaFreteResponse
import br.com.zup.torresmath.ErrorDetails
import br.com.zup.torresmath.FretesServiceGrpc
import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesServiceGrpc::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {
        logger.info("Calculando frete para: $request")

        val cep = request?.cep

        if (cep == null || cep.isBlank()) {
//            val e = StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("cep deve ser informado"))
            val e = Status.INVALID_ARGUMENT
                .withDescription("CEP deve ser informado")
                .asRuntimeException()

            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("CEP inválido")
                .augmentDescription("Formato esperado deve ser 99999-999")
                .asRuntimeException()

            responseObserver?.onError(e)
        }

        // Simulação de verificação de segurança

        if (cep.endsWith("1")) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Usuario não pode acessar esse recurso")
                .addDetails(
                    Any.pack(
                        ErrorDetails.newBuilder()
                            .setCode(401)
                            .setMessage("Token expirado")
                            .build()
                    )
                )
                .build()

            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        var valor = 0.0

        try {
            valor = Random.nextDouble(from = 0.0, until = 100.0)

            if (valor > 50.0)
                throw IllegalStateException("Erro inesperado")

        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e) // Anexado ao Status, mas não é enviado ao Client
                    .asRuntimeException()
            )
        }

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: ${response.valor}")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()
    }
}