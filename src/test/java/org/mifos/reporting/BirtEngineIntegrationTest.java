package org.mifos.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@Slf4j
class BirtEngineIntegrationTest {

    private static IReportEngine engine = null;
    private static IReportEngineFactory factory = null;

    @BeforeAll
    static void startEngine() {
        try {
            EngineConfig config = new EngineConfig();
            // Tell BIRT to use the SLF4J logger we just configured
            config.setLogConfig(null, Level.WARNING);

            // 1. Start the OSGi Platform
            log.info("Attempting to start Eclipse BIRT Platform...");
            Platform.startup(config);

            // 2. Create the Report Engine Factory
            IReportEngineFactory factory =
                    (IReportEngineFactory)
                            Platform.createFactoryObject(
                                    IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

            // 3. Create the Engine
            engine = factory.createReportEngine(config);
            log.info("BIRT Engine Started Successfully!");

        } catch (Exception e) {
            log.error("Failed to start BIRT Engine", e);
            fail("BIRT Engine failed to start: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopEngine() {
        if (engine != null) {
            engine.destroy();
        }
        Platform.shutdown();
        log.info("BIRT Engine Shutdown.");
    }

    @Test
    void testRenderHelloWorldPdf() throws Exception {
        assertNotNull(engine, "Engine should be initialized");

        // 1. Load the report design
        InputStream reportStream = new ClassPathResource("hello_world.rptdesign").getInputStream();
        IReportRunnable design = engine.openReportDesign(reportStream);

        // 2. Create a task to run and render the report
        IRunAndRenderTask task = engine.createRunAndRenderTask(design);

        // 3. Set PDF Render options
        PDFRenderOption options = new PDFRenderOption();
        options.setOutputFormat("pdf");

        // Render to a byte array (in memory)
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        options.setOutputStream(pdfOutput);
        task.setRenderOption(options);

        // 4. Run!
        log.info("Starting Render Task...");
        task.run();
        task.close();

        // 5. Verify we actually got a PDF
        byte[] pdfBytes = pdfOutput.toByteArray();
        log.info("Render Complete. Output Size: {} bytes", pdfBytes.length);

        assertTrue(pdfBytes.length > 0, "PDF output should not be empty");
        // Verify PDF Header signature (%PDF)
        assertEquals(0x25, pdfBytes[0], "File should start with %");
        assertEquals(0x50, pdfBytes[1], "File should start with P");
        assertEquals(0x44, pdfBytes[2], "File should start with D");
        assertEquals(0x46, pdfBytes[3], "File should start with F");
    }
}