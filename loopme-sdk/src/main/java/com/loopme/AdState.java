package com.loopme;

/**
 * Enum with all possible ad states.
 */
enum AdState {
	/**
	 * Initial state of ad right after creation.
	 * Can be also after onHide() notification or destroy().
	 */
	NONE,
	
	/**
	 * Ad currently in "loading" process.
	 * Can be between trigger load() and onLoadSuccess(), onLoadFail() notifications or destroy().
	 * While Ad in this state all other calling `load` methods will be ignored
	 */
	LOADING,
	
	/**
	 * Ad currently displays on screen.
	 * Can be between trigger show() and onHide() notification or destroy()
	 */
	SHOWING;
}