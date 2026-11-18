package br.com.mindconnect.geografia;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AprovacaoCoordenadasRequest(

        @NotNull(message = "A latitude é obrigatória.")
        @DecimalMin(value = "-90.0", message = "A latitude deve ser maior ou igual a -90.")
        @DecimalMax(value = "90.0", message = "A latitude deve ser menor ou igual a 90.")
        BigDecimal latitude,

        @NotNull(message = "A longitude é obrigatória.")
        @DecimalMin(value = "-180.0", message = "A longitude deve ser maior ou igual a -180.")
        @DecimalMax(value = "180.0", message = "A longitude deve ser menor ou igual a 180.")
        BigDecimal longitude,

        @Pattern(
                regexp = "CNES|GEOAPIFY",
                message = "A origem deve ser CNES ou GEOAPIFY."
        )
        String origem

) {
}
