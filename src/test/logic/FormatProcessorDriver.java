package test.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import logic.FormatProcessor;

import objects.Command;
import test.InvalidUserInput;


public class FormatProcessorDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String CurLine = ""; // Line read from standard in

		System.out.println("Enter a line of text (type 'quit' to exit): ");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		Command cmd = null;

		while (!(CurLine.equals("quit"))) {
			try {
				CurLine = in.readLine();

				cmd = FormatProcessor.processUserInput(CurLine);
				if (cmd.getStartDate() != null) {
					System.out.println(cmd.getStartDate().getTime());
				} else {
					System.out.println("Empty Date");
				}

				if (cmd.getEndDate() != null) {
					System.out.println(cmd.getEndDate().getTime());
				} else {
					System.out.println("Empty Date");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidUserInput e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!(CurLine.equals("quit"))) {
				System.out.println("You typed Command Type: "
						+ cmd.getCommandType());
				System.out.println("You typed: " + cmd.getDetail());
				System.out.println("You typed Priority: " + cmd.getPriority());
				System.out.println("You typed getSearchType: "
						+ cmd.getSearchType());
				// if(cmd.getSortKeyWords().get(0)!=null)
				// {
				// System.out.println("You typed PgetSortKeyWords: " +
				// cmd.getSortKeyWords().get(0));
				// }
			}
		}
	}

}
