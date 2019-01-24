package org.freejava.checkstyle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import antlr.collections.AST;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class UnhandledExceptionCheck extends AbstractCheck {

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.LITERAL_CATCH };
	}

	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	public void visitToken(DetailAST ast) {
		try {
			DetailAST paramDefBlock = ast
					.findFirstToken(TokenTypes.PARAMETER_DEF);
			DetailAST typeBlock = paramDefBlock.findFirstToken(TokenTypes.TYPE);
			AST identBlock = typeBlock.getNextSibling();
			String exceptionVarName = identBlock.getText();

			// Find statements like LOGGER.info(.., ex) or throw ...
			boolean found = findLoggingStatement(ast, exceptionVarName);
			if (!found) {
				found = findThrowStatement(ast);
			}
			if (!found) {
				log(ast.getLineNo(), ast.getColumnNo(), "Exception "
						+ exceptionVarName + " is not logged correctly.");
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			log(ast.getLineNo(), ast.getColumnNo(), sw.toString());
		}
	}

	private boolean findThrowStatement(DetailAST ast) {
		List<AST> asts = findAstByType(ast, TokenTypes.LITERAL_THROW);
		return (!asts.isEmpty());
	}

	private boolean findLoggingStatement(DetailAST catchAst, String exceptionVarName) {
		boolean result = false;

		String loggerName = findLoggerName(catchAst);

		// Find all METHOD_CALL which has first IDENT equals to loggerName and
		// exceptionVarName in param list
		List<AST> asts = findAstByType(catchAst, TokenTypes.METHOD_CALL);
		for (AST ast : asts) {
			if (ast.getFirstChild().getType() == TokenTypes.DOT
					&& ast.getFirstChild().getFirstChild().getType() == TokenTypes.IDENT) {
				String ident = ast.getFirstChild().getFirstChild().getText();
				if (ident.equals(loggerName) || (loggerName == null && ident.toLowerCase().indexOf("log") != -1)) {
					AST elist = ast.getFirstChild().getNextSibling();
					if (elist.getType() == TokenTypes.ELIST) {
						List<AST> exprs = new ArrayList<AST>();
						AST child2 = elist.getFirstChild();
						while (child2 != null) {
							if (child2.getType() == TokenTypes.EXPR) {
								exprs.add(child2);
							}
							child2 = child2.getNextSibling();
						}
						if (exprs.size() > 1
								&& exprs.get(1).getFirstChild().getType() == TokenTypes.IDENT) {
							String param2Name = exprs.get(1).getFirstChild()
									.getText();
							if (param2Name.equals(exceptionVarName)) {
								result = true;
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}

	private List<AST> findAstByType(AST ast, int tokenType) {
		List<AST> result = new ArrayList<AST>();
		// current node
		if (ast.getType() == tokenType) {
			result.add(ast);
		}
		// child nodes
		AST child = ast.getFirstChild();
		while (child != null) {
			result.addAll(findAstByType(child, tokenType));
			child = child.getNextSibling();
		}
		return result;
	}

	private String findLoggerName(DetailAST ast) {
		String result = null;
		DetailAST currentAst = ast;
		while (currentAst != null) {
			if (currentAst.getType() == TokenTypes.CLASS_DEF) {
				result = findLoggerNameInClass(currentAst);
				if (result != null)  break;
			}
			currentAst = currentAst.getParent();
		}
		return result;
	}

	private String findLoggerNameInClass(DetailAST ast) {
		String result = null;
		DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
		AST child = objBlock.getFirstChild();
		while (result == null && child != null) {
			if (child.getType() == TokenTypes.VARIABLE_DEF) {
				DetailAST typeBlock = ((DetailAST) child)
						.findFirstToken(TokenTypes.TYPE);
				DetailAST typeIdentBlock = typeBlock
						.findFirstToken(TokenTypes.IDENT);
				if (typeIdentBlock != null) {
					String typeIdent = typeIdentBlock.getText();
					AST identBlock = typeBlock.getNextSibling();
					String varIdent = identBlock.getText();
					String simpleTypeName = typeIdent.substring(typeIdent
							.lastIndexOf('.') + 1);
					if (simpleTypeName.indexOf("Log") != -1) {
						result = varIdent;
					}
				}
			}
			child = child.getNextSibling();
		}
		return result;
	}
}
