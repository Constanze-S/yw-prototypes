package org.yesworkflow.model;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yesworkflow.annotations.Begin;
import org.yesworkflow.annotations.End;
import org.yesworkflow.annotations.In;
import org.yesworkflow.annotations.Out;
import org.yesworkflow.annotations.Return;

public class WorkflowBuilder {
		
        private Begin beginAnnotation;
        private End endAnnotation;
        private List<Port> workflowInPorts = new LinkedList<Port>();
        private List<Port> workflowOutPorts = new LinkedList<Port>();
        private List<Port> workflowReturnPorts = new LinkedList<Port>();

		private List<Program> nestedPrograms = new LinkedList<Program>();
        private List<Channel> nestedChannels = new LinkedList<Channel>();
        private List<Function> nestedFunctions = new LinkedList<Function>();
		private Map<String,List<Port>> nestedProgramInPorts = new LinkedHashMap<String,List<Port>>();
		private Map<String,Port> nestedProgramOutPorts = new  LinkedHashMap<String,Port>();
        private Map<String,Program> programForName = new HashMap<String,Program>();
        
        @SuppressWarnings("unused")
        private PrintStream stdoutStream = null;
        
        @SuppressWarnings("unused")
        private PrintStream stderrStream = null;

        public WorkflowBuilder(PrintStream stdoutStream, PrintStream stderrStream) {
            this.stdoutStream = stdoutStream;
            this.stderrStream = stderrStream;
        }
        
		public WorkflowBuilder begin(Begin annotation) {
			this.beginAnnotation = annotation;
			return this;
		}

        public void end(End annotation) {
            this.endAnnotation = annotation;
        }

		public String getProgramName() {
			return beginAnnotation.name;
		}
		
		public WorkflowBuilder nestedProgram(Program program) {
			this.nestedPrograms.add(program);
			this.programForName.put(program.beginAnnotation.name, program);
			return this;
		}

        public Port inPort(In inAnnotation) throws Exception {
            
            // model the outward facing in port
            Port inPort = new Port(inAnnotation, beginAnnotation);
            workflowInPorts.add(inPort);
            
            // model a corresponding, inward-facing out port
            Port outPort = new Port(inAnnotation, beginAnnotation);
            nestedOutPort(outPort);

            // return the outward facing port
            return inPort;
        }
        
        public Port outPort(Out outAnnotation) {

            // model the outward facing out port
            Port outPort = new Port(outAnnotation, beginAnnotation);
            workflowOutPorts.add(outPort);
            
            // model a corresponding, inward-facing in port
            Port inPort = new Port(outAnnotation, beginAnnotation);
            nestedInPort(inPort);
            
            // return the outward facing port
            return outPort;
        }

        public Port returnPort(Return returnAnnotation) {

            // model the outward facing out port
            Port returnPort = new Port(returnAnnotation, beginAnnotation);
            workflowReturnPorts.add(returnPort);
            
            // model a corresponding, inward-facing in port
            Port inPort = new Port(returnAnnotation, beginAnnotation);
            nestedInPort(inPort);
            
            // return the outward facing port
            return returnPort;
        }
        
		public WorkflowBuilder nestedInPort(Port inPort) {
		    String binding = inPort.flowAnnotation.binding();
			addNestedInport(binding, inPort);
			return this;
		}

		private void addNestedInport(String binding, Port inPort) {
			List<Port> ports = this.nestedProgramInPorts.get(binding);
			if (ports == null) {
				ports = new LinkedList<Port>();
				this.nestedProgramInPorts.put(binding, ports);
			}
			ports.add(inPort);
		}
		
		public WorkflowBuilder nestedOutPort(Port outPort) throws Exception {
			
			String binding = outPort.flowAnnotation.binding();
			
			// ensure no other writers to this @out binding
			if (nestedProgramOutPorts.containsKey(binding)) {
				throw new Exception("Multiple @out comments bound to " + binding);
			}
			
			// store the @out comment
			this.nestedProgramOutPorts.put(binding, outPort);

			return this;
		}

		public boolean hasNestedPrograms() {
		    return this.nestedPrograms.size() > 0;
		}
		
		public Function buildFunction() throws Exception {	            
            pruneUnusedNestedProgramInPorts();
            pruneUnusedNestedProgramOutPorts();
            buildNestedChannels();
		    return new Function(
		            beginAnnotation,
                    endAnnotation,
                    workflowInPorts,
                    workflowOutPorts,
                    workflowReturnPorts,
                    nestedPrograms,
                    nestedChannels,
                    nestedFunctions
		            );
        }

		
        public Program buildProgram() throws Exception {
            return new Program(
                    beginAnnotation, 
                    endAnnotation, 
                    workflowInPorts, 
                    workflowOutPorts
                    );
        }
	            
        public Workflow buildWorkflow() throws Exception {
			
			pruneUnusedNestedProgramInPorts();
			pruneUnusedNestedProgramOutPorts();
			buildNestedChannels();
			
			return new Workflow(
                    beginAnnotation,
                    endAnnotation,
                    workflowInPorts,
                    workflowOutPorts,
                    nestedPrograms,
                    nestedChannels,
                    nestedFunctions
		            );
		}
	
        private void pruneUnusedNestedProgramInPorts() {

            List<String> unmatchedInBindings = new LinkedList<String>();
            for (Map.Entry<String, List<Port>> entry : nestedProgramInPorts.entrySet()) {
                String binding = entry.getKey();
                if (!workflowInPorts.contains(binding) && !nestedProgramOutPorts.containsKey(binding)) {
                    unmatchedInBindings.add(binding);
                }
            }
            for (String binding : unmatchedInBindings) nestedProgramInPorts.remove(binding);
        }
	    
        private void pruneUnusedNestedProgramOutPorts() {

            List<String> unmatchedOutBindings = new LinkedList<String>();
            
            for (Entry<String, Port> entry : nestedProgramOutPorts.entrySet()) {
                String binding = entry.getKey();
                if (!workflowOutPorts.contains(binding) && !nestedProgramInPorts.containsKey(binding)) {
                    unmatchedOutBindings.add(binding);
                }
            }
            
            for (String binding : unmatchedOutBindings) nestedProgramOutPorts.remove(binding);
        }	
        
        private void buildNestedChannels() throws Exception {
            
            // build the channels between in and out ports
            for (Map.Entry<String, List<Port>> entry : nestedProgramInPorts.entrySet()) {
    
                String binding = entry.getKey();
                List<Port> boundInPorts = entry.getValue();
    
                // get information about the @out port that writes to this binding
                Port boundOutPort = nestedProgramOutPorts.get(binding);
                if (boundOutPort == null) throw new Exception("No @out corresponding to @in " + binding);
                
                String outProgramName = boundOutPort.beginAnnotation.name;
                Program outProgram = programForName.get(outProgramName);
                
                // iterate over @in ports that bind to the current @out port
                for (Port inPort : boundInPorts) {
                
                    // get information about this @in port
                    String inProgramName = inPort.beginAnnotation.name;
                    Program inProgram = programForName.get(inProgramName);
    
                    // store the new channel
                    Channel channel = new Channel(outProgram, boundOutPort, inProgram, inPort);
                    nestedChannels.add(channel);
                }   
            }
        }
	}