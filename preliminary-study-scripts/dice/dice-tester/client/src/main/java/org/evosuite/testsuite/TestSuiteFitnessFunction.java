/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testsuite;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.assertion.ArgumentValueTraceEntry;
import org.evosuite.assertion.ArgumentValueTraceObserver;
import org.evosuite.assertion.NullTraceEntry;
import org.evosuite.assertion.NullTraceObserver;
import org.evosuite.assertion.PrimitiveTraceEntry;
import org.evosuite.assertion.PrimitiveTraceObserver;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract TestSuiteFitnessFunction class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class TestSuiteFitnessFunction extends
        FitnessFunction<AbstractTestSuiteChromosome<? extends ExecutableChromosome>> {

	private static final long serialVersionUID = 7243635497292960457L;

	/** Constant <code>logger</code> */
	protected static final Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);


	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @return Result of the execution
	 */
	@Deprecated
	public ExecutionResult runTest(TestCase test) {
		ExecutionResult result = new ExecutionResult(test, null);
		
		PrimitiveTraceObserver primitiveObserver = new PrimitiveTraceObserver();
		NullTraceObserver nullObserver = new NullTraceObserver();
		ArgumentValueTraceObserver argsValueObserver = new ArgumentValueTraceObserver();
		
		TestCaseExecutor.getInstance().addObserver(primitiveObserver);
		TestCaseExecutor.getInstance().addObserver(nullObserver);
		TestCaseExecutor.getInstance().addObserver(argsValueObserver);
//		logger.warn("Number of observers : " + TestCaseExecutor.getInstance().getExecutionObservers().size());

		try {
			result = TestCaseExecutor.getInstance().execute(test);
			MaxStatementsStoppingCondition.statementsExecuted(result.getExecutedStatements());
		} catch (Exception e) {
			logger.warn("TG: Exception caught: " + e.getMessage(), e);
			try {
				Thread.sleep(1000);
				result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			} catch (Exception e1) {
				throw new Error(e1);
			}

		}
		
		result.setTrace(primitiveObserver.getTrace(), PrimitiveTraceEntry.class);
		result.setTrace(nullObserver.getTrace(), NullTraceEntry.class);
		result.setTrace(argsValueObserver.getTrace(), ArgumentValueTraceEntry.class);
		
		TestCaseExecutor.getInstance().removeObserver(primitiveObserver);
		TestCaseExecutor.getInstance().removeObserver(nullObserver);
		TestCaseExecutor.getInstance().removeObserver(argsValueObserver);

		// System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}

	/**
	 * <p>
	 * runTestSuite
	 * </p>
	 * 
	 * @param suite
	 *            a {@link org.evosuite.testsuite.AbstractTestSuiteChromosome}
	 *            object.
	 * @return a {@link java.util.List} object.
	 */
	protected List<ExecutionResult> runTestSuite(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();

		for (ExecutableChromosome chromosome : suite.getTestChromosomes()) {
			// Only execute test if it hasn't been changed
			if (chromosome.isChanged() || chromosome.getLastExecutionResult() == null) {
				ExecutionResult result = chromosome.executeForFitnessFunction(this);

				if (result != null) {
					results.add(result);

					chromosome.setLastExecutionResult(result); // .clone();
					chromosome.setChanged(false);
				}
			} else {
				results.add(chromosome.getLastExecutionResult());
			}
		}
		suite.setChanged(false);

		return results;
	}



	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}
}
