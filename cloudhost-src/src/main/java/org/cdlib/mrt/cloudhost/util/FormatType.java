/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cdlib.mrt.cloudhost.util;

import org.cdlib.mrt.utility.StringUtil;

/**
  * Shortcut enum for format types for both State display and Archive response
  */
 public enum FormatType
 {
     anvl("state", "txt", "text/x-anvl", null),
     json("state", "json", "application/json", null),
     serial("state", "ser", "application/x-java-serialized-object", null),
     octet("file", "txt", "application/octet-stream", null),
     targz("archive", "tar.gz", "application/x-tar-gz", "gzip"),
     tar("archive", "tar", "application/x-tar", null),
     txt("file", "txt", "plain/text", null),
     xml("state", "xml", "text/xml", null),
     rdf("state", "xml", "application/rdf+xml", null),
     turtle("state", "ttl", "text/turtle", null),
     xhtml("state", "xhtml", "application/xhtml+xml", null),
     zip("archive", "zip", "application/zip", null);

     protected final String form;
     protected final String extension;
     protected final String mimeType;
     protected final String encoding;

     FormatType(String form, String extension, String mimeType, String encoding) {
         this.form = form;
         this.extension = extension;
         this.mimeType = mimeType;
         this.encoding = encoding;
     }

     /**
      * Extension for this format
      * @return
      */
     public String getExtension() {
         return extension;
     }

     /**
      * return MIME type of this format response
      * @param t
      * @return MIME type
      */
     public String getMimeType() {
         return mimeType;
     }

     /**
      * return form of this format
      * @param t
      * @return MIME type
      */
     public String getForm() {
         return form;
     }

     /**
      * return encoding of this format
      * @return encoding
      */
     public String getEncoding() {
         return encoding;
     }

     public static FormatType containsExtension(String t)
     {
         if (StringUtil.isEmpty(t)) return null;
         for (FormatType p : FormatType.values()) {
             if (t.contains("." + p.getExtension())) {
                 return p;
             }
         }
         return null;
     }

     public static FormatType valueOfExtension(String t)
     {
         if (StringUtil.isEmpty(t)) return null;
         for (FormatType p : FormatType.values()) {
             if (p.getExtension().equals(t)) {
                 return p;
             }
         }
         return null;
     }

     /**
      * return MIME type of this format response
      * @param t
      * @return MIME type
      */
     public static FormatType valueOfMimeType(String t)
     {
         if (StringUtil.isEmpty(t)) return null;
         for (FormatType p : FormatType.values()) {
             if (p.getMimeType().equals(t)) {
                 return p;
             }
         }
         return null;
     }
 }
