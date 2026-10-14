package pe.eda.aulas.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pe.eda.aulas.dto.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RecursoDuplicadoException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> manejarDuplicado(
            RecursoDuplicadoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "Los datos enviados no son válidos",
                errores
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> manejarFormato(
            HttpMessageNotReadableException ex) {
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud tiene un formato incorrecto o contiene valores no permitidos",
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> manejarIntegridad(
            DataIntegrityViolationException ex) {
        return respuesta(
                HttpStatus.CONFLICT,
                "La operación entra en conflicto con los datos existentes",
                List.of()
        );
    }

    private org.springframework.http.ResponseEntity<ErrorResponse> respuesta(
            HttpStatus estado,
            String mensaje,
            List<String> errores) {
        ErrorResponse cuerpo = new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                mensaje,
                errores
        );
        return org.springframework.http.ResponseEntity
                .status(estado)
                .body(cuerpo);
    }
}
