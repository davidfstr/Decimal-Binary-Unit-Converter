import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.*;

import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.builder.*;

public class DecBinUnitConverter extends JApplet {
	private static final Color SRC_BGCOLOR_VALID = Color.GREEN;
	private static final Color SRC_BGCOLOR_INVALID = Color.RED.brighter();
	
	private List<Unit> units = new ArrayList<Unit>();
	private volatile boolean ignoreModifications = false;
	private Unit activeUnit = null;
	
	private JCheckBox useCommas;
	
	// Must be public for applet support
	public DecBinUnitConverter() {}
	
	/* Application */
	
	public static void main(String[] args) {
		new DecBinUnitConverter().run();
	}
	
	private void run() {
		JFrame frame = new JFrame("Decimal-Binary Unit Converter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(createUI());
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
	}
	
	/* Applet */
	
	public void init() {
		this.setContentPane(createUI());
	}
	
	/* Program */
	
	private Container createUI() {
		FormLayout layout = new FormLayout(
			"right:p, 4dlu, p, 14dlu," +	// columns
				"right:p, 4dlu, p, 14dlu," +
				"right:p, 4dlu, p",
			"");							// add rows dynamically
		
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		
		appendUnit(builder, "GiB", "Gebibyte", "1024^3 bytes", 1L*1024*1024*1024*8);
		appendUnit(builder, "MiB", "Mebibyte", "1024^2 bytes", 1L*1024*1024*8);
		appendUnit(builder, "KiB", "Kibibyte", "1024 bytes", 1L*1024*8);
		builder.nextLine();
		appendUnit(builder, "GB", "Gigabyte", "1000^3 bytes", 1L*1000*1000*1000*8);
		appendUnit(builder, "MB", "Megabyte", "1000^2 bytes", 1L*1000*1000*8);
		appendUnit(builder, "KB", "Kilobyte", "1000 bytes", 1L*1000*8);
		builder.nextLine();
		
		builder.appendRow("14dlu");
		builder.nextLine();
		
		appendUnit(builder, "Gib(it)", "Gebibit", "1024^3 bits", 1L*1024*1024*1024);
		appendUnit(builder, "Mib(it)", "Mebibit", "1024^2 bits", 1L*1024*1024);
		appendUnit(builder, "Kib(it)", "Kibibit", "1024 bits", 1L*1024);
		builder.nextLine();
		appendUnit(builder, "Gb(it)", "Gigabit", "1000^3 bits", 1L*1000*1000*1000);
		appendUnit(builder, "Mb(it)", "Megabit", "1000^2 bits", 1L*1000*1000);
		appendUnit(builder, "Kb(it)", "Kilobit", "1000 bits", 1L*1000);
		builder.nextLine();
		
		builder.appendRow("14dlu");
		builder.nextLine();
		
		appendUnit(builder, "Bytes", "Byte", "8 bits", 1L*8, true);
		builder.nextLine();
		appendUnit(builder, "Bits", "Bit", "1 bit", 1L, true);
		builder.nextLine();
		
		builder.appendSeparator();
		builder.append(createOptions(), 6);
		
		return builder.getPanel();
	}
	
	private void appendUnit(
		DefaultFormBuilder builder,
		String acronym, String name, String description, long bitsPerUnit)
	{
		appendUnit(builder, acronym, name, description, bitsPerUnit, false);
	}
	
	private void appendUnit(
		DefaultFormBuilder builder,
		String acronym, String name, String description, long bitsPerUnit,
		boolean fillRow)
	{
		String tooltipText = name + " (" + description + ")";
		
		JLabel label = new JLabel(acronym);
		label.setToolTipText(tooltipText);
		
		JTextField field = new JTextField(9);
		field.setToolTipText(tooltipText);
		
		builder.append(label);
		builder.append(field, fillRow ? 9 : 1);
		
		final Unit thisUnit = new Unit(field, bitsPerUnit);
		units.add(thisUnit);
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent evt) { documentChanged(); }
			public void insertUpdate(DocumentEvent evt) { documentChanged(); }
			public void removeUpdate(DocumentEvent evt) { documentChanged(); }
			
			private void documentChanged() {
				if (ignoreModifications) return;
				
				activeUnit = thisUnit;
				updateFields();
			}
		});
	}
	
	private Container createOptions() {
		useCommas = new JCheckBox("Use Commas");
		useCommas.setSelected(true);
		useCommas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateFields();
			}
		});
		
		Box content = Box.createHorizontalBox();
		content.setBackground(Color.GRAY);
		content.add(useCommas);
		return content;
	}
	
	private void updateFields() {
		if (activeUnit == null) return;
		
		// Reset all fields to the default background color
		for (Unit u : units) {
			u.field.setBackground(Color.WHITE);
		}
		
		try {
			double bits = Double.parseDouble(activeUnit.field.getText().replaceAll(",", "")) * activeUnit.bitsPerUnit;
			
			activeUnit.field.setBackground(SRC_BGCOLOR_VALID);
			try {
				ignoreModifications = true;
				
				String prefixFormat = useCommas.isSelected() ? "#,###" : "#";
				String suffixFormat = "##";
				DecimalFormat formatter = new DecimalFormat(prefixFormat + "." + suffixFormat);
				for (Unit u : units) {
					if (u == activeUnit) continue;
					
					u.field.setText(formatter.format(bits / u.bitsPerUnit));
				}
			} finally {
				ignoreModifications = false;
			}
		} catch (NumberFormatException e) {
			activeUnit.field.setBackground(SRC_BGCOLOR_INVALID);
		}
	}
}

class Unit {
	JTextField field;
	long bitsPerUnit;
	
	Unit(JTextField field, long bitsPerUnit) {
		this.field = field;
		this.bitsPerUnit = bitsPerUnit;
	}
}
