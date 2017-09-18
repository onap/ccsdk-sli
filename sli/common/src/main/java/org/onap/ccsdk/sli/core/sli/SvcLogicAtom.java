/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.sli;

public class SvcLogicAtom extends SvcLogicExpression {

	public enum AtomType {
		NUMBER,
		STRING,
		IDENTIFIER,
		CONTEXT_VAR

	}

	private AtomType atomType;
	private String atom;


	public SvcLogicAtom(String atomType, String atom)
	{
		this.atomType = AtomType.valueOf(atomType);
		this.atom = atom;

	}

	public SvcLogicAtom(String atom)
	{

		if (atom == null)
		{
			this.atomType = null;
			this.atom = null;
		}
		else
		{
			if (atom.startsWith("$"))
			{
				this.atomType = AtomType.CONTEXT_VAR;
				this.atom = atom.substring(1);
			}
			else
			{
				if (Character.isDigit(atom.charAt(0)))
				{
					this.atomType = AtomType.NUMBER;
					this.atom = atom;
				}
				else if (atom.charAt(0) == '\'')
				{
					this.atomType = AtomType.STRING;
					this.atom = atom.substring(1, atom.length()-1);
				}
				else
				{
					this.atomType = AtomType.IDENTIFIER;
					this.atom = atom;

				}

			}
		}
	}

	public AtomType getAtomType() {
		return atomType;
	}

	public void setAtomType(String newType)
	{
		atomType = AtomType.valueOf(newType);
	}

	public String getAtom() {
		return atom;
	}



	public void setAtomType(AtomType atomType) {
		this.atomType = atomType;
	}

	public void setAtom(String atom) {
		this.atom = atom;
	}



	public String toString()
	{
		StringBuffer sbuff = new StringBuffer();
		switch(getAtomType())
		{
			case CONTEXT_VAR:
				sbuff.append("$");
			case IDENTIFIER:
				boolean needDot = false;
				for (SvcLogicExpression term: this.getOperands())
				{
					if (needDot)
					{
						sbuff.append(".");
					}
					sbuff.append(term.toString());
					needDot = true;
				}
				return sbuff.toString();
			case STRING:
			case NUMBER:
			default:
				return atom;
		}
	}

	public String asParsedExpr()
	{
		// simplify debugging output for NUMBER type
		if (atomType == AtomType.NUMBER) {
			return atom;
		}

		StringBuffer sbuff = new StringBuffer();

		sbuff.append("(atom");
		sbuff.append("<");
		sbuff.append(atomType.toString());
		sbuff.append(">");

		switch(atomType)
		{
			case IDENTIFIER:
			case CONTEXT_VAR:
				for (SvcLogicExpression term : getOperands())
				{
					sbuff.append(" ");
					sbuff.append(term.asParsedExpr());

				}
				break;
			default:
				sbuff.append(" ");
				sbuff.append(atom);
		}

		sbuff.append(")");
		return sbuff.toString();
	}



}
