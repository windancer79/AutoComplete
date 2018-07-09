/*
 * 12/21/2008
 *
 * ParameterizedCompletionDescriptionToolTip.java - A "tool tip" displaying
 * information on the function or method currently being entered.
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;


/**
 * A "tool tip" that displays information on the function or method currently
 * being entered.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ParameterizedCompletionDescriptionToolTip {

    /**
     * The actual tool tip.
     */
    private JWindow tooltip;

    /**
     * The label that holds the description.
     */
    private JLabel descLabel;

    /**
     * The completion being described.
     */
    private ParameterizedCompletion pc;

    private JTextComponent comp;

    /**
     * Constructor.
     *
     * @param owner The parent window.
     * @param ac    The parent auto-completion.
     * @param pc    The completion being described.
     */
    public ParameterizedCompletionDescriptionToolTip(Window owner,
                                                     ParameterizedCompletionContext context,
                                                     AutoCompletion ac, ParameterizedCompletion pc) {

        tooltip = new JWindow(owner);

        this.pc = pc;
        this.comp = ac.getTextComponent();
        descLabel = new JLabel();
        descLabel.setBorder(BorderFactory.createCompoundBorder(
                TipUtil.getToolTipBorder(),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        descLabel.setOpaque(true);
        //descLabel.setBackground(TipUtil.getToolTipBackground());
        descLabel.setBackground(Color.lightGray);
        // It appears that if a JLabel is set as a content pane directly, when
        // using the JDK's opacity API's, it won't paint its background, even
        // if label.setOpaque(true) is called.  You have to have a container
        // underneath it for it to paint its background.  Thus, we embed our
        // label in a parent JPanel to handle this case.
        //tooltip.setContentPane(descLabel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(descLabel);
        tooltip.setContentPane(panel);

        // Give apps a chance to decorate us with drop shadows, etc.
        PopupWindowDecorator decorator = PopupWindowDecorator.get();
        if (decorator != null) {
            decorator.decorate(tooltip);
        }

        updateText(0);

        tooltip.setFocusableWindowState(false);

    }


    /**
     * Returns whether this tool tip is visible.
     *
     * @return Whether this tool tip is visible.
     * @see #setVisible(boolean)
     */
    public boolean isVisible() {
        return tooltip.isVisible();
    }


    /**
     * Sets the location of this tool tip relative to the given rectangle.
     *
     * @param r The visual position of the caret (in screen coordinates).
     */
    public void setLocationRelativeTo(Rectangle r) {

        // Multi-monitor support - make sure the completion window (and
        // description window, if applicable) both fit in the same window in
        // a multi-monitor environment.  To do this, we decide which monitor
        // the rectangle "r" is in, and use that one (just pick top-left corner
        // as the defining point).
        Rectangle screenBounds = Util.getScreenBoundsForPoint(r.x, r.y);
        //Dimension screenSize = tooltip.getToolkit().getScreenSize();

        // Try putting our stuff "above" the caret first.
        int y = r.y - 5 - tooltip.getHeight();
        if (y < 0) {
            y = r.y + r.height + 5;
        }

        // Get x-coordinate of completions.  Try to align left edge with the
        // caret first.
        int x = r.x;
        if (x < screenBounds.x) {
            x = screenBounds.x;
        } else if (x + tooltip.getWidth() > screenBounds.x + screenBounds.width) { // completions don't fit
            x = screenBounds.x + screenBounds.width - tooltip.getWidth();
        }

        tooltip.setLocation(x, y);

    }


    /**
     * Toggles the visibility of this tool tip.
     *
     * @param visible Whether this tool tip should be visible.
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        tooltip.setVisible(visible);
    }


    /**
     * Updates the text in the tool tip to have the current parameter
     * displayed in bold.
     *
     * @param selectedParam The index of the selected parameter.
     * @return Whether the text needed to be updated.
     */
    public boolean updateText(int selectedParam) {
        boolean isFirstParamHidden = false;
        String fullText = pc.getProvider().getAlreadyEnteredFullLineText(this.comp);
        if (fullText.indexOf('.') > 0) {
            isFirstParamHidden = true;
        }
        int paramCount = pc.getParamCount();
        int internalParamCount = paramCount;
        int i = 0;
        if (isFirstParamHidden) {
            internalParamCount--;
        }
        StringBuilder sb = new StringBuilder("<html>");

        for (; i < internalParamCount; i++) {
            //System.out.println("startIndex = " + String.valueOf(i) );
            if (i == selectedParam) {
                sb.append("<b>");
            }else{
                sb.append("<span style=\'color:#666666;\'>");
            }
            int internalParamIndex = isFirstParamHidden ? i + 1 : i;
            // Some parameter types may have chars in them unfriendly to HTML
            // (such as type parameters in Java).  We need to take care to
            // escape these.
            String temp = pc.getParam(internalParamIndex).toString();
            sb.append(RSyntaxUtilities.escapeForHtml(temp, "<br>", false));

            if (i == selectedParam) {
                sb.append("</b>");
            }else{
                sb.append("</span>");
            }
            if (i < internalParamCount - 1) {
                sb.append(pc.getProvider().getParameterListSeparator());
            }
        }

//        if (selectedParam >= 0 && selectedParam < internalParamCount) {
//            int internalSelectedIndex = isFirstParamHidden ? selectedParam + 1 : selectedParam;
//            ParameterizedCompletion.Parameter param =
//                    pc.getParam(internalSelectedIndex);
//            String desc = param.getDescription();
//            if (desc != null) {
//                sb.append("<br>");
//                sb.append(desc);
//            }
//        }
        System.out.println("updatetext = " + sb.toString());
        descLabel.setText(sb.toString());
        tooltip.pack();

        return true;

    }


    /**
     * Updates the <tt>LookAndFeel</tt> of this window and the description
     * window.
     */
    public void updateUI() {
        SwingUtilities.updateComponentTreeUI(tooltip);
    }


}