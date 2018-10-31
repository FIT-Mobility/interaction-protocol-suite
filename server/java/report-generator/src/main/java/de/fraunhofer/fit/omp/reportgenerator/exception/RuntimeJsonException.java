package de.fraunhofer.fit.omp.reportgenerator.exception;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 04.04.2018
 */
public class RuntimeJsonException extends RuntimeException {

    private static final long serialVersionUID = -7376114738678111027L;

    public RuntimeJsonException(Exception e) {
        super(e);
    }
}
