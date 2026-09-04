package eu.orchestrator.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Logger;

@Controller
@ControllerAdvice
class GlobalDefaultExceptionHandler {

    public static final String DEFAULT_ERROR_VIEW = "error";
    private static final Logger logger = Logger
            .getLogger(GlobalDefaultExceptionHandler.class.getName());

    @ExceptionHandler(Exception.class)
    public ResponseEntity genericException(final Exception e) {
//        logger.info("Message3: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
    }

}
