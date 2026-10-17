package com.knowledge.sporedoc

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * SD-23 R2/R10: entering crop mode hides the normal footer's `crop` box and replaces it with
 * SAVE/CANCEL, each with its own content description, per the Architect's named string
 * resources (`viewer_footer_crop_content_description`, `crop_save_label`,
 * `crop_save_content_description`, `crop_cancel_content_description`,
 * `crop_save_error_message`).
 */
class CropFooterLabelsStringsTest {

    private fun stringsXml(): String = File("src/main/res/values/strings.xml").readText()

    @Test
    fun `a string resource exists for the normal footer's crop box content description`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string name=\"viewer_footer_crop_content_description\"> resource",
            xml.contains("name=\"viewer_footer_crop_content_description\"")
        )
    }

    @Test
    fun `a string resource exists for the crop footer's SAVE label`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string name=\"crop_save_label\"> resource",
            xml.contains("name=\"crop_save_label\"")
        )
    }

    @Test
    fun `a string resource exists for the crop footer's SAVE content description`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string name=\"crop_save_content_description\"> resource",
            xml.contains("name=\"crop_save_content_description\"")
        )
    }

    @Test
    fun `a string resource exists for the crop footer's CANCEL content description`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string name=\"crop_cancel_content_description\"> resource",
            xml.contains("name=\"crop_cancel_content_description\"")
        )
    }

    @Test
    fun `a string resource exists for the crop save error message`() {
        val xml = stringsXml()
        assertTrue(
            "Expected a <string name=\"crop_save_error_message\"> resource",
            xml.contains("name=\"crop_save_error_message\"")
        )
    }
}
