package objects;
/**
 * @author      Jiao Jing Ping
 * @version     0.2                                   
 * @since       2011-11-1    
 * 
 * COMMAND_TYPE is a enum class which
 * mainly help differentiate Command's CommandType  
 * 
 * To use, call COMMAND_TYPE.determineCommandType(Command command) 
 * It will return one of the predefined COMMAND_TYPE from the enum
 *     
 */
public enum COMMAND_TYPE {
	ADD, GET, SORT, SET, DELETEOPEN, DELETECOMPLETE, SEARCHOPEN, GETCOMPLETE, GETOPEN, INVALID, COMPLETE, INCOMPLETE, SEARCHCOMPLETE, SAVE, BACKUP;

	public static COMMAND_TYPE determineCommandType(Command command) {
		String commandTypeString = command.getCommandType();
		return identifyCommandByString(commandTypeString);
	}

	public static COMMAND_TYPE identifyCommandByString(String commandTypeString) {
		if (commandTypeString == null)
			throw new Error("command type string cannot be null!");

		if (commandTypeString.equalsIgnoreCase("add")) {
			return COMMAND_TYPE.ADD;
		} else if (commandTypeString.equalsIgnoreCase("sort")) {
			return COMMAND_TYPE.SORT;
		} else if (commandTypeString.equalsIgnoreCase("set")) {
			return COMMAND_TYPE.SET;
		} else if (commandTypeString.equalsIgnoreCase("deleteopen")) {
			return COMMAND_TYPE.DELETEOPEN;
		} else if (commandTypeString.equalsIgnoreCase("deletecomplete")) {
			return COMMAND_TYPE.DELETECOMPLETE;
		} else if (commandTypeString.equalsIgnoreCase("searchOpen")) {
			return COMMAND_TYPE.SEARCHOPEN;
		} else if (commandTypeString.equalsIgnoreCase("searchClose")) {
			return COMMAND_TYPE.SEARCHCOMPLETE;
		} else if (commandTypeString.equalsIgnoreCase("getCompleteTasks")
				|| commandTypeString.equalsIgnoreCase("getComplete")
				|| commandTypeString.equalsIgnoreCase("getCompleteTask")) {
			return COMMAND_TYPE.GETCOMPLETE;
		} else if (commandTypeString.equalsIgnoreCase("getOpenTasks")
				|| commandTypeString.equalsIgnoreCase("getOpen")
				|| commandTypeString.equalsIgnoreCase("getOpenTask")) {
			return COMMAND_TYPE.GETOPEN;
		} else if (commandTypeString.equalsIgnoreCase("complete")) {
			return COMMAND_TYPE.COMPLETE;
		} else if (commandTypeString.equalsIgnoreCase("incomplete")) {
			return COMMAND_TYPE.INCOMPLETE;
		} else if (commandTypeString.equalsIgnoreCase("save")) {
			return COMMAND_TYPE.SAVE;
		} else if (commandTypeString.equalsIgnoreCase("get")) {
			return COMMAND_TYPE.GET;
		} else if (commandTypeString.equalsIgnoreCase("backup")) {
			return COMMAND_TYPE.BACKUP;
		} else
			return COMMAND_TYPE.INVALID;
	}

}
