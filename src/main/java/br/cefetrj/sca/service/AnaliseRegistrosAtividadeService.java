package br.cefetrj.sca.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.cefetrj.sca.dominio.Aluno;
import br.cefetrj.sca.dominio.Curso;
import br.cefetrj.sca.dominio.Professor;
import br.cefetrj.sca.dominio.VersaoCurso;
import br.cefetrj.sca.dominio.atividadecomplementar.EnumEstadoAtividadeComplementar;
import br.cefetrj.sca.dominio.atividadecomplementar.RegistroAtividadeComplementar;
import br.cefetrj.sca.dominio.inclusaodisciplina.Comprovante;
import br.cefetrj.sca.dominio.repositories.AlunoRepositorio;
import br.cefetrj.sca.dominio.repositories.CursoRepositorio;
import br.cefetrj.sca.dominio.repositories.ProfessorRepositorio;
import br.cefetrj.sca.dominio.repositories.RegistroAtividadeComplementarRepositorio;
import br.cefetrj.sca.service.util.DadosFormPesquisaAtividades;
import br.cefetrj.sca.service.util.SolicitaRegistroAtividadesResponse;

@Component
public class AnaliseRegistrosAtividadeService {
	
	@Autowired
	private AlunoRepositorio alunoRepo;
	
	@Autowired
	private RegistroAtividadeComplementarRepositorio regRepo;
	
	@Autowired
	private ProfessorRepositorio professorRepositorio;
	
	@Autowired
	private CursoRepositorio cursoRepo;
	
	public List<String> obterVersoesCurso(String siglaCurso){
		
		List<String> versoesCursos = new ArrayList<>();
		for(VersaoCurso versaoCurso: cursoRepo.findAllVersaoCursoByCurso(siglaCurso)){
			versoesCursos.add(versaoCurso.getNumero());
		}
		
		return versoesCursos;
	}
	
	public DadosFormPesquisaAtividades homeAnaliseAtividades(String matricula){
		Professor professor = getProfessorByMatricula(matricula);
		
		Set<String> siglaCursos = new HashSet<String>();
		//TODO
		//cursoRepo.findAll() trocar por recuperação de cursos dos quais o professor é coordenador de atividades complementares
		for(Curso curso: cursoRepo.findAll()){
			siglaCursos.add(curso.getSigla());
		}
		
		DadosFormPesquisaAtividades dadosAnaliseAtividades = 
				new DadosFormPesquisaAtividades(professor.getNome(),professor.getMatricula(),
						siglaCursos,EnumEstadoAtividadeComplementar.values());
		
		return dadosAnaliseAtividades;
	}
	
	public SolicitaRegistroAtividadesResponse listarRegistrosAtividade(String siglaCurso,
			String numeroVersao, String status){
		
		VersaoCurso versaoCurso = cursoRepo.getVersaoCurso(siglaCurso, numeroVersao);
		List<Aluno> alunos = alunoRepo.findAllByVersaoCurso(versaoCurso);
		
		SolicitaRegistroAtividadesResponse response = new SolicitaRegistroAtividadesResponse();
		for (Aluno aluno : alunos) {
			for (RegistroAtividadeComplementar reg : 
				aluno.getRegistrosAtiv(EnumEstadoAtividadeComplementar.findByText(status))) {
				response.add(reg, aluno);
			}
		}
		return response;
	}
	
	public void atualizaStatusRegistro(String matriculaAluno, Long idRegistro, String status){
		
		Aluno aluno = getAlunoPorMatricula(matriculaAluno);
		RegistroAtividadeComplementar registroAtiv = regRepo.findRegistroAtividadeComplementarById(idRegistro);
		
		if(aluno.podeTerRegistroDeferido(registroAtiv)){
			registroAtiv.setEstado(EnumEstadoAtividadeComplementar.findByText(status));
		}

		regRepo.save(registroAtiv);
	}

	public Comprovante getComprovante(String matriculaAluno, Long idReg) {
		
		Aluno aluno = getAlunoPorMatricula(matriculaAluno);
		return aluno.getRegistroAtiv(idReg).getDocumento();
	}
	
	private Professor getProfessorByMatricula(String matricula){
		return professorRepositorio.findProfessorByMatricula(matricula);
	}
	
	private Aluno getAlunoPorMatricula(String matriculaAluno) {
		if (matriculaAluno == null || matriculaAluno.trim().equals("")) {
			throw new IllegalArgumentException("Matrícula deve ser fornecida!");
		}

		Aluno aluno = null;

		try {
			aluno = alunoRepo.findAlunoByMatricula(matriculaAluno);
		} catch (Exception e) {
			throw new IllegalArgumentException("Aluno não encontrado ("
					+ matriculaAluno + ")", e);
		}

		return aluno;
	}	
}
