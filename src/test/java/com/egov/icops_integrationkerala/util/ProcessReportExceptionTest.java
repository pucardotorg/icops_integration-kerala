package com.egov.icops_integrationkerala.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProcessReportExceptionTest {

    @Mock
    private ProcessReportException processReportException;

    @Test
    public void testProcessReportException() {
        processReportException = new ProcessReportException("message");
    }
}
