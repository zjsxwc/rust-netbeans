/**
 * Copyright (C) 2013 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.drrb.rust.netbeans;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

@MimeRegistration(mimeType = RustLanguage.MIME_TYPE, service = CompletionProvider.class)
public class RustCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent textComponent) {

        if (queryType == CompletionProvider.COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new RustCompletionQuery(), textComponent);
        } else {
            return null;
        }
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0; //Only offer completion when it's explicitly requested
    }

    private static class RustCompletionQuery extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {
            String filter = null;
            int startOffset = caretOffset - 1;

            try {
                final int lineStartOffset = DocUtil.getRowFirstNonWhite(document, caretOffset);
                final char[] line = document.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                final int whiteOffset = indexOfWhite(line);
                filter = new String(line, whiteOffset + 1, line.length - whiteOffset - 1);
                if (whiteOffset > 0) {
                    startOffset = lineStartOffset + whiteOffset + 1;
                } else {
                    startOffset = lineStartOffset;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            for (RustKeyword keyword : RustKeyword.values()) {
                if (keyword.image().startsWith(filter)) {
                    completionResultSet.addItem(new RustCompletionItem(keyword.image(), startOffset, caretOffset));
                }
            }
            completionResultSet.finish();
        }
    }

    static int indexOfWhite(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
}