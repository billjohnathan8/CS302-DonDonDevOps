package com.dondondevops.payment.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    @RequestMapping("/error")
    public ProblemDetail handleError(HttpServletRequest request) {
        var status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        var statusCode = HttpStatus.resolve(Integer.parseInt(status.toString()));
        statusCode = statusCode == null ? HttpStatus.INTERNAL_SERVER_ERROR : statusCode;
        switch (statusCode) {
            case NOT_FOUND: return ProblemDetail.forStatusAndDetail(statusCode, "Resource not found");
            default: return ProblemDetail.forStatusAndDetail(statusCode, "Something went wrong");
        }
    }
}
