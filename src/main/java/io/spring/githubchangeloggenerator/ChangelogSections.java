/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.githubchangeloggenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.spring.githubchangeloggenerator.github.payload.Issue;

import org.springframework.util.CollectionUtils;

/**
 * Manages sections of the changelog report.
 *
 * @author Phillip Webb
 */
class ChangelogSections {

	private static final List<ChangelogSection> DEFAULT_SECTIONS;
	static {
		List<ChangelogSection> sections = new ArrayList<>();
		add(sections, ":star: New Features", "enhancement");
		add(sections, ":beetle: Bug Fixes", "bug", "regression");
		add(sections, ":notebook_with_decorative_cover: Documentation", "documentation");
		add(sections, ":hammer: Dependency Upgrades", "dependency-upgrade");
		DEFAULT_SECTIONS = Collections.unmodifiableList(sections);
	}

	private static void add(List<ChangelogSection> sections, String title, String... labels) {
		sections.add(new ChangelogSection(title, labels));
	}

	private final List<ChangelogSection> sections;

	ChangelogSections(ApplicationProperties properties) {
		this.sections = adapt(properties.getSections());
	}

	private List<ChangelogSection> adapt(List<ApplicationProperties.Section> propertySections) {
		if (CollectionUtils.isEmpty(propertySections)) {
			return DEFAULT_SECTIONS;
		}
		return propertySections.stream().map(this::adapt).collect(Collectors.toList());
	}

	private ChangelogSection adapt(ApplicationProperties.Section propertySection) {
		return new ChangelogSection(propertySection.getTitle(), propertySection.getLabels());
	}

	Map<ChangelogSection, List<Issue>> collate(List<Issue> issues) {
		SortedMap<ChangelogSection, List<Issue>> collated = new TreeMap<>(Comparator.comparing(this.sections::indexOf));
		for (Issue issue : issues) {
			ChangelogSection section = getSection(issue);
			if (section != null) {
				collated.computeIfAbsent(section, (key) -> new ArrayList<>());
				collated.get(section).add(issue);
			}
		}
		return collated;
	}

	private ChangelogSection getSection(Issue issue) {
		for (ChangelogSection section : this.sections) {
			if (section.isMatchFor(issue)) {
				return section;
			}
		}
		return null;
	}

}