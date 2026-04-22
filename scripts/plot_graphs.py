#!/usr/bin/env python3
"""
Generate poster-quality graphs from the Memento dataset.

Requirements:
    pip install matplotlib pandas numpy

Usage:
    python scripts/plot_graphs.py

Output:
    scripts/graphs/poster.png        — combined poster (all 6 graphs)
    scripts/graphs/01_age.png        — individual panels
    scripts/graphs/02_gender.png
    scripts/graphs/03_region.png
    scripts/graphs/04_life_expectancy.png
    scripts/graphs/05_phases.png
    scripts/graphs/06_percent_lived.png
"""

import os
import sys
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.gridspec import GridSpec
from collections import Counter

# ── Paths ──────────────────────────────────────────────────────────────────────

BASE      = os.path.dirname(os.path.abspath(__file__))
CSV_PATH  = os.path.join(BASE, "users_dataset.csv")
OUT_DIR   = os.path.join(BASE, "graphs")
os.makedirs(OUT_DIR, exist_ok=True)

# ── Palette & style ────────────────────────────────────────────────────────────

VIOLET  = "#7C3AED"
PINK    = "#EC4899"
AMBER   = "#F59E0B"
EMERALD = "#10B981"
BLUE    = "#3B82F6"
RED     = "#EF4444"
ORANGE  = "#F97316"
TEAL    = "#14B8A6"
INDIGO  = "#6366F1"
ROSE    = "#F43F5E"
CYAN    = "#06B6D4"
SLATE   = "#64748B"

PALETTE = [VIOLET, PINK, AMBER, EMERALD, BLUE, RED, ORANGE, TEAL, INDIGO, ROSE, CYAN, SLATE]

BG      = "#FFFFFF"
PANEL   = "#F8F7FF"   # very light lavender panel bg
GRID    = "#E5E7EB"
TEXT    = "#1E1B4B"
MUTED   = "#6B7280"

plt.rcParams.update({
    "font.family":        "DejaVu Sans",
    "figure.facecolor":   BG,
    "axes.facecolor":     PANEL,
    "axes.edgecolor":     GRID,
    "axes.labelcolor":    TEXT,
    "axes.titlecolor":    TEXT,
    "axes.titlepad":      14,
    "axes.titlesize":     15,
    "axes.labelsize":     11,
    "axes.grid":          True,
    "axes.grid.axis":     "y",
    "grid.color":         GRID,
    "grid.linewidth":     0.8,
    "xtick.color":        MUTED,
    "ytick.color":        MUTED,
    "xtick.labelsize":    9,
    "ytick.labelsize":    9,
    "legend.fontsize":    9,
    "legend.framealpha":  0.9,
    "legend.edgecolor":   GRID,
})

def style_ax(ax, title, xlabel="", ylabel=""):
    ax.set_title(title, fontsize=15, fontweight="bold", color=TEXT, pad=14)
    ax.set_xlabel(xlabel, color=MUTED, fontsize=10)
    ax.set_ylabel(ylabel, color=MUTED, fontsize=10)
    ax.spines[["top", "right"]].set_visible(False)
    ax.spines[["left", "bottom"]].set_color(GRID)
    ax.tick_params(colors=MUTED)

def save(fig, name):
    path = os.path.join(OUT_DIR, name)
    fig.savefig(path, dpi=180, bbox_inches="tight",
                facecolor=BG, edgecolor="none")
    print(f"Saved -> {path}")

# ── Load data ──────────────────────────────────────────────────────────────────

df = pd.read_csv(CSV_PATH)
print(f"Loaded {len(df)} users from {CSV_PATH}\n")

# ── Graph 1 — Age distribution ─────────────────────────────────────────────────

def plot_age(ax):
    bins = range(16, 94, 4)
    n, edges, patches = ax.hist(df["age"], bins=bins, color=VIOLET,
                                edgecolor="white", linewidth=0.6, zorder=3)
    # colour bars by age group
    for patch, left in zip(patches, edges):
        t = (left - 16) / (92 - 16)
        patch.set_facecolor(plt.cm.cool(0.2 + t * 0.7))

    # KDE overlay
    from numpy import linspace
    from scipy.stats import gaussian_kde
    kde  = gaussian_kde(df["age"], bw_method=0.25)
    xs   = linspace(16, 92, 300)
    ys   = kde(xs) * len(df) * 4          # scale to match histogram
    ax.plot(xs, ys, color=VIOLET, linewidth=2.5, zorder=4, label="KDE")

    style_ax(ax, "Age Distribution", "Age", "Number of Users")
    ax.set_xlim(14, 94)
    ax.set_xticks(range(16, 94, 8))
    ax.axvline(df["age"].mean(), color=ROSE, linewidth=1.8,
               linestyle="--", zorder=5, label=f"Mean {df['age'].mean():.1f}")
    ax.legend()

# ── Graph 2 — Gender slider ────────────────────────────────────────────────────

def plot_gender(ax):
    counts = df["genderSlider"].value_counts().sort_index()

    # gradient: blue → pink
    gradient = [plt.cm.cool(i / 9) for i in range(10)]
    bars = ax.bar(counts.index, counts.values, color=gradient,
                  edgecolor="white", linewidth=0.8, zorder=3, width=0.75)

    # value labels on top of bars
    for bar, val in zip(bars, counts.values):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 4,
                str(val), ha="center", va="bottom",
                fontsize=8.5, color=TEXT, fontweight="bold")

    style_ax(ax, "Gender Slider Distribution", "Slider Position", "Number of Users")
    ax.set_xticks(range(10))
    ax.set_xticklabels(
        ["0\n(Male)", "1", "2", "3", "4", "5", "6", "7", "8", "9\n(Female)"],
        fontsize=8.5
    )
    ax.set_xlim(-0.6, 9.6)

    # annotation bracket for "in-between"
    in_between = counts[1:9].sum()
    ax.annotate(
        f"In-between\n{in_between} users ({in_between/10:.1f}%)",
        xy=(4.5, counts[4:6].max()), xytext=(4.5, counts.max() * 0.6),
        ha="center", fontsize=8, color=TEAL, fontweight="bold",
        arrowprops=dict(arrowstyle="-", color=TEAL, lw=1.2),
    )

# ── Graph 3 — Users by region (donut) ─────────────────────────────────────────

def plot_region(ax):
    region_counts = df["region"].value_counts()
    colors = [VIOLET, AMBER, EMERALD, BLUE, ORANGE, PINK]

    wedges, texts, autotexts = ax.pie(
        region_counts.values,
        labels=region_counts.index,
        autopct="%1.1f%%",
        colors=colors,
        startangle=140,
        pctdistance=0.78,
        wedgeprops=dict(width=0.55, edgecolor="white", linewidth=2),
        textprops=dict(color=TEXT, fontsize=9),
    )
    for at in autotexts:
        at.set_fontsize(8.5)
        at.set_fontweight("bold")
        at.set_color("white")

    # centre label
    ax.text(0, 0, f"{len(df)}\nusers", ha="center", va="center",
            fontsize=13, fontweight="bold", color=TEXT)

    ax.set_title("Users by Region", fontsize=15, fontweight="bold",
                 color=TEXT, pad=14)

# ── Graph 4 — Life expectancy by region (horizontal violin) ───────────────────

def plot_life_expectancy(ax):
    regions  = df["region"].value_counts().index.tolist()   # sorted by size
    data     = [df[df["region"] == r]["lifeExpectancy"].values for r in regions]
    colors   = [VIOLET, AMBER, EMERALD, BLUE, ORANGE, PINK]

    parts = ax.violinplot(data, vert=False, showmedians=True,
                          showextrema=False)

    for i, (pc, col) in enumerate(zip(parts["bodies"], colors)):
        pc.set_facecolor(col)
        pc.set_alpha(0.75)
        pc.set_edgecolor("white")
        pc.set_linewidth(1.2)

    parts["cmedians"].set_color("white")
    parts["cmedians"].set_linewidth(2)

    # overlay scatter
    for i, (vals, col) in enumerate(zip(data, colors), start=1):
        jitter = np.random.RandomState(i).uniform(-0.18, 0.18, len(vals))
        ax.scatter(vals, np.full_like(vals, i, dtype=float) + jitter,
                   color=col, alpha=0.25, s=8, zorder=3)

    style_ax(ax, "Life Expectancy by Region", "Life Expectancy (years)", "")
    ax.set_yticks(range(1, len(regions) + 1))
    ax.set_yticklabels(regions, fontsize=9)
    ax.set_xlim(50, 92)
    ax.grid(axis="x", color=GRID, linewidth=0.8)
    ax.grid(axis="y", visible=False)

# ── Graph 5 — Phase templates (horizontal bar) ────────────────────────────────

def plot_phases(ax):
    tmpl_counts = df["phaseTemplate"].value_counts()
    colors      = PALETTE[:len(tmpl_counts)]

    bars = ax.barh(tmpl_counts.index, tmpl_counts.values,
                   color=colors, edgecolor="white",
                   linewidth=0.6, height=0.7, zorder=3)

    for bar, val in zip(bars, tmpl_counts.values):
        ax.text(val + 2, bar.get_y() + bar.get_height() / 2,
                str(val), va="center", fontsize=8.5,
                color=TEXT, fontweight="bold")

    style_ax(ax, "Life Phase Templates", "Number of Users", "")
    ax.set_xlim(0, tmpl_counts.max() * 1.15)
    ax.invert_yaxis()
    ax.grid(axis="x", color=GRID, linewidth=0.8)
    ax.grid(axis="y", visible=False)
    ax.set_axisbelow(True)

# ── Graph 6 — % of life lived distribution ────────────────────────────────────

def plot_percent_lived(ax):
    pct = df["percentLived"]

    bins = list(range(0, 101, 5))
    n, edges, patches = ax.hist(pct, bins=bins, color=EMERALD,
                                edgecolor="white", linewidth=0.6, zorder=3)

    # colour from green (young) to amber (old)
    for patch, left in zip(patches, edges):
        t = left / 100
        r = int(0x10 + t * (0xF5 - 0x10))
        g = int(0xB9 + t * (0x9E - 0xB9))
        b = int(0x81 + t * (0x0B - 0x81))
        patch.set_facecolor(f"#{r:02X}{g:02X}{b:02X}")

    style_ax(ax, "Life Progress Distribution", "% of Life Lived", "Number of Users")
    ax.set_xlim(-2, 102)
    ax.set_xticks(range(0, 101, 10))
    ax.axvline(50, color=ROSE, linewidth=1.8, linestyle="--",
               zorder=5, label="50 %")
    ax.axvline(pct.mean(), color=VIOLET, linewidth=1.8,
               linestyle=":", zorder=5, label=f"Mean {pct.mean():.1f}%")
    ax.legend()

# ── Compose poster ─────────────────────────────────────────────────────────────

def build_poster():
    fig = plt.figure(figsize=(24, 14), facecolor=BG)
    gs  = GridSpec(2, 3, figure=fig,
                   hspace=0.45, wspace=0.35,
                   left=0.06, right=0.97,
                   top=0.88, bottom=0.07)

    axes = [
        fig.add_subplot(gs[0, 0]),
        fig.add_subplot(gs[0, 1]),
        fig.add_subplot(gs[0, 2]),
        fig.add_subplot(gs[1, 0]),
        fig.add_subplot(gs[1, 1]),
        fig.add_subplot(gs[1, 2]),
    ]

    plot_age(axes[0])
    plot_gender(axes[1])
    plot_region(axes[2])
    plot_life_expectancy(axes[3])
    plot_phases(axes[4])
    plot_percent_lived(axes[5])

    # Poster title
    fig.text(0.5, 0.945, "Memento — Life in Weeks: User Dataset Analysis",
             ha="center", va="center",
             fontsize=22, fontweight="bold", color=TEXT)
    fig.text(0.5, 0.915, "Synthetic dataset  |  n = 1,000 users  |  82 countries",
             ha="center", va="center",
             fontsize=12, color=MUTED)

    # subtle top bar accent
    fig.add_artist(plt.Rectangle((0, 0.965), 1, 0.035,
                                 transform=fig.transFigure,
                                 color=VIOLET, zorder=0))
    fig.text(0.5, 0.982, "MEMENTO", ha="center", va="center",
             fontsize=11, fontweight="bold", color="white",
             transform=fig.transFigure)

    return fig


# ══════════════════════════════════════════════════════════════════════════════
# POSTER 2 — Advanced visualisations
# ══════════════════════════════════════════════════════════════════════════════

# ── Graph 7 — Population pyramid ──────────────────────────────────────────────

def plot_pyramid(ax):
    male   = df[df["genderSlider"] == 0]["age"]
    female = df[df["genderSlider"] == 9]["age"]

    edges  = list(range(16, 90, 6))
    labels = [f"{e}–{e+5}" for e in edges]
    m_cnt  = np.histogram(male,   bins=edges + [90])[0].astype(float)
    f_cnt  = np.histogram(female, bins=edges + [90])[0].astype(float)
    y      = np.arange(len(labels))

    ax.barh(y, -m_cnt, color=BLUE,  edgecolor="white", linewidth=0.5,
            height=0.75, label="Male (0)", zorder=3)
    ax.barh(y,  f_cnt, color=PINK,  edgecolor="white", linewidth=0.5,
            height=0.75, label="Female (9)", zorder=3)

    max_v = max(m_cnt.max(), f_cnt.max())
    ax.set_xlim(-max_v * 1.25, max_v * 1.25)
    ax.set_yticks(y)
    ax.set_yticklabels(labels, fontsize=8.5)
    ax.axvline(0, color=GRID, linewidth=1.2)

    # x-axis labels as absolute values
    ticks = ax.get_xticks()
    ax.set_xticklabels([str(int(abs(t))) for t in ticks], fontsize=8.5)

    style_ax(ax, "Age Pyramid  (Male vs Female)", "Number of Users", "Age Group")
    ax.grid(axis="x", color=GRID, linewidth=0.8)
    ax.grid(axis="y", visible=False)
    ax.legend(loc="lower right", fontsize=8.5)

# ── Graph 8 — Region bubble chart ─────────────────────────────────────────────

def plot_bubbles(ax):
    regions = df["region"].unique()
    colors  = [VIOLET, AMBER, EMERALD, BLUE, ORANGE, PINK]

    for region, col in zip(regions, colors):
        sub   = df[df["region"] == region]
        x     = sub["age"].mean()
        y     = sub["lifeExpectancy"].mean()
        size  = len(sub)
        pct   = sub["percentLived"].mean()

        ax.scatter(x, y, s=size * 2.2, color=col, alpha=0.82,
                   edgecolors="white", linewidths=1.5, zorder=3)
        ax.text(x, y, region, ha="center", va="center",
                fontsize=7.5, fontweight="bold", color="white", zorder=4)
        # small count label below
        ax.text(x, y - (size ** 0.5) * 0.072, f"n={size}",
                ha="center", va="top", fontsize=7, color=col)

    style_ax(ax, "Region Overview  (bubble = user count)",
             "Average Age", "Average Life Expectancy (years)")
    ax.set_xlim(30, 65)
    ax.grid(color=GRID, linewidth=0.8)

# ── Graph 9 — Life expectancy lollipop (top 20 countries) ─────────────────────

def plot_lollipop(ax):
    top20 = (df.groupby("country")["lifeExpectancy"]
               .mean()
               .sort_values()
               .tail(20))

    cmap   = plt.cm.plasma
    colors = [cmap(i / (len(top20) - 1)) for i in range(len(top20))]
    y      = np.arange(len(top20))

    ax.hlines(y, 55, top20.values, color=GRID, linewidth=1.2, zorder=2)
    ax.scatter(top20.values, y, color=colors, s=70, zorder=4, edgecolors="white",
               linewidths=0.8)

    ax.set_yticks(y)
    ax.set_yticklabels(top20.index, fontsize=8)
    ax.set_xlim(55, 91)
    ax.axvline(df["lifeExpectancy"].mean(), color=ROSE, linewidth=1.6,
               linestyle="--", zorder=3,
               label=f"Dataset mean {df['lifeExpectancy'].mean():.1f}")

    style_ax(ax, "Life Expectancy — Top 20 Countries",
             "Avg Life Expectancy (years)", "")
    ax.grid(axis="x", color=GRID, linewidth=0.8)
    ax.grid(axis="y", visible=False)
    ax.legend(fontsize=8.5)

# ── Graph 10 — Radar: regional comparison ─────────────────────────────────────

def plot_radar(ax):
    metrics = ["Avg Age", "Avg Life Exp", "% Lived", "Phase Count", "Weeks Left"]
    N       = len(metrics)
    angles  = np.linspace(0, 2 * np.pi, N, endpoint=False).tolist()
    angles += angles[:1]   # close the loop

    regions = df["region"].value_counts().index.tolist()
    colors  = [VIOLET, AMBER, EMERALD, BLUE, ORANGE, PINK]

    agg = df.groupby("region").agg(
        avg_age         =("age",            "mean"),
        avg_le          =("lifeExpectancy",  "mean"),
        avg_pct         =("percentLived",    "mean"),
        avg_phases      =("phaseCount",      "mean"),
        avg_weeks_left  =("weeksRemaining",  "mean"),
    )

    # Normalise each metric 0–1 across regions
    norm = (agg - agg.min()) / (agg.max() - agg.min() + 1e-9)

    for region, col in zip(regions, colors):
        vals = norm.loc[region].tolist() + norm.loc[region].tolist()[:1]
        ax.plot(angles, vals, color=col, linewidth=2, label=region)
        ax.fill(angles, vals, color=col, alpha=0.08)

    ax.set_xticks(angles[:-1])
    ax.set_xticklabels(metrics, fontsize=9, color=TEXT)
    ax.set_yticklabels([])
    ax.set_ylim(0, 1)
    ax.spines["polar"].set_color(GRID)
    ax.grid(color=GRID, linewidth=0.8)
    ax.set_facecolor(PANEL)
    ax.set_title("Regional Profile Radar", fontsize=15,
                 fontweight="bold", color=TEXT, pad=18)
    ax.legend(loc="upper right", bbox_to_anchor=(1.35, 1.12), fontsize=8.5)

# ── Graph 11 — Life grid (average user) ───────────────────────────────────────

PHASE_COLORS = {
    "Childhood":        "#3B82F6",
    "School":           "#10B981",
    "University":       "#F59E0B",
    "Bachelor's":       "#F59E0B",
    "Master's":         "#F97316",
    "PhD":              "#EF4444",
    "Career":           "#8B5CF6",
    "Academic Career":  "#6366F1",
    "Senior":           "#64748B",
    "Retirement":       "#64748B",
    "Young Adult":      "#F59E0B",
    "Family Years":     "#EC4899",
    "Empty Nest":       "#14B8A6",
    "Startup Years":    "#F97316",
    "Growth Phase":     "#8B5CF6",
    "Legacy":           "#F59E0B",
    "Education":        "#10B981",
    "Early Retirement": "#14B8A6",
    "Emerging":         "#EC4899",
    "Established":      "#8B5CF6",
    "Military Service": "#64748B",
    "Civilian Career":  "#8B5CF6",
    "Residency":        "#F43F5E",
    "Exploration":      "#06B6D4",
    "Settled Career":   "#8B5CF6",
    "Growing Up":       "#3B82F6",
    "Adult Life":       "#8B5CF6",
    "Youth":            "#3B82F6",
    "Adulthood":        "#8B5CF6",
}

def plot_life_grid(ax):
    avg_user   = df.iloc[0].copy()        # use seed-0 representative user
    avg_age    = int(df["age"].mean())
    avg_le     = int(df["lifeExpectancy"].mean())
    weeks_lived = avg_age * 52

    # Build default phase schedule (app defaults, relative to week 0)
    schedule = [
        ("Childhood",    0,       6 * 52),
        ("School",       6 * 52,  18 * 52),
        ("University",   18 * 52, 23 * 52),
        ("Career",       23 * 52, 65 * 52),
        ("Senior",       65 * 52, avg_le * 52),
    ]

    WEEKS_PER_ROW = 52
    total_weeks   = avg_le * 52
    total_rows    = avg_le

    xs, ys, face_colors, edge_colors = [], [], [], []

    for week in range(total_weeks):
        row = week // WEEKS_PER_ROW
        col = week  % WEEKS_PER_ROW

        # Determine phase colour
        phase_col = "#CCCCCC"
        for name, start, end in schedule:
            if start <= week < end:
                phase_col = PHASE_COLORS.get(name, "#CCCCCC")
                break

        past    = week < weeks_lived
        current = week == weeks_lived

        if current:
            fc = "#FFFFFF"
            ec = ROSE
        elif past:
            fc = phase_col
            ec = "none"
        else:
            # future: very faint tint of the phase colour
            r = int(phase_col[1:3], 16)
            g = int(phase_col[3:5], 16)
            b = int(phase_col[5:7], 16)
            # blend toward white
            r2 = int(r + (255 - r) * 0.78)
            g2 = int(g + (255 - g) * 0.78)
            b2 = int(b + (255 - b) * 0.78)
            fc = f"#{r2:02X}{g2:02X}{b2:02X}"
            ec = "none"

        xs.append(col)
        ys.append(total_rows - row)    # flip so age 0 at top
        face_colors.append(fc)
        edge_colors.append(ec)

    # Draw as scatter squares
    ax.scatter(xs, ys, c=face_colors, marker="s", s=8, linewidths=0.4,
               edgecolors=edge_colors, zorder=3)

    # Highlight current week
    cw_col = weeks_lived % WEEKS_PER_ROW
    cw_row = total_rows - weeks_lived // WEEKS_PER_ROW
    ax.scatter([cw_col], [cw_row], c=ROSE, marker="s", s=14,
               linewidths=0, zorder=5)

    # Phase legend
    legend_patches = [
        mpatches.Patch(color=c, label=n)
        for n, c in [
            ("Childhood",   "#3B82F6"),
            ("School",      "#10B981"),
            ("University",  "#F59E0B"),
            ("Career",      "#8B5CF6"),
            ("Senior",      "#64748B"),
        ]
    ]
    legend_patches.append(mpatches.Patch(color=ROSE, label=f"Now (age {avg_age})"))
    ax.legend(handles=legend_patches, loc="lower right",
              fontsize=7.5, ncol=3, framealpha=0.9)

    ax.set_xlim(-1, WEEKS_PER_ROW + 1)
    ax.set_ylim(-1, total_rows + 2)
    ax.set_xlabel("Week of Year", color=MUTED, fontsize=10)
    ax.set_ylabel("Age (years)", color=MUTED, fontsize=10)
    ax.set_xticks([0, 13, 26, 39, 51])
    ax.set_xticklabels(["Jan", "Apr", "Jul", "Oct", "Dec"], fontsize=8.5)

    # Y ticks: every 10 years
    ytick_vals = [total_rows - y * 52 for y in range(0, avg_le + 1, 10)]
    ytick_lbls = [str(y) for y in range(0, avg_le + 1, 10)]
    ax.set_yticks(ytick_vals)
    ax.set_yticklabels(ytick_lbls, fontsize=8.5)

    ax.set_title(
        f"Life in Weeks — Average User  (age {avg_age}, LE {avg_le} yrs)",
        fontsize=15, fontweight="bold", color=TEXT, pad=14,
    )
    ax.set_facecolor(PANEL)
    ax.spines[["top", "right"]].set_visible(False)
    ax.spines[["left", "bottom"]].set_color(GRID)
    ax.grid(False)

# ── Compose poster 2 ───────────────────────────────────────────────────────────

def build_poster2():
    fig = plt.figure(figsize=(26, 15), facecolor=BG)
    gs  = GridSpec(2, 3, figure=fig,
                   hspace=0.48, wspace=0.38,
                   left=0.05, right=0.97,
                   top=0.88, bottom=0.06)

    ax_pyramid  = fig.add_subplot(gs[0, 0])
    ax_bubble   = fig.add_subplot(gs[0, 1])
    ax_lollipop = fig.add_subplot(gs[0, 2])
    ax_radar    = fig.add_subplot(gs[1, 0], polar=True)
    ax_grid     = fig.add_subplot(gs[1, 1:])    # spans columns 1–2

    plot_pyramid(ax_pyramid)
    plot_bubbles(ax_bubble)
    plot_lollipop(ax_lollipop)
    plot_radar(ax_radar)
    plot_life_grid(ax_grid)

    fig.text(0.5, 0.945, "Memento — Life in Weeks: Advanced Data Insights",
             ha="center", va="center",
             fontsize=22, fontweight="bold", color=TEXT)
    fig.text(0.5, 0.915,
             "Synthetic dataset  |  n = 1,000 users  |  82 countries",
             ha="center", va="center", fontsize=12, color=MUTED)

    fig.add_artist(plt.Rectangle((0, 0.965), 1, 0.035,
                                 transform=fig.transFigure,
                                 color=VIOLET, zorder=0))
    fig.text(0.5, 0.982, "MEMENTO", ha="center", va="center",
             fontsize=11, fontweight="bold", color="white",
             transform=fig.transFigure)

    return fig


def build_individual(plot_fn, filename, figsize=(9, 5.5)):
    fig, ax = plt.subplots(figsize=figsize, facecolor=BG)
    if filename == "03_region.png":
        fig, ax = plt.subplots(figsize=(7, 7), facecolor=BG,
                               subplot_kw=dict(aspect="equal"))
    elif filename == "04_life_expectancy.png":
        fig, ax = plt.subplots(figsize=(10, 5.5), facecolor=BG)
    plot_fn(ax)
    fig.tight_layout(pad=2)
    return fig


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    try:
        from scipy.stats import gaussian_kde   # noqa — imported inside plot_age
    except ImportError:
        print("scipy not found — installing...")
        import subprocess
        subprocess.check_call([sys.executable, "-m", "pip", "install", "scipy", "-q"])

    np.random.seed(42)

    graphs = [
        ("01_age.png",              plot_age,              (9,   5.5)),
        ("02_gender.png",           plot_gender,           (9,   5.5)),
        ("03_region.png",           plot_region,           (7,   7  )),
        ("04_life_expectancy.png",  plot_life_expectancy,  (10,  5.5)),
        ("05_phases.png",           plot_phases,           (9,   5.5)),
        ("06_percent_lived.png",    plot_percent_lived,    (9,   5.5)),
    ]

    print("Generating poster 1 panels...")
    for filename, fn, fsize in graphs:
        kw = dict(subplot_kw=dict(aspect="equal")) if "region" in filename else {}
        fig, ax = plt.subplots(figsize=fsize, facecolor=BG, **kw)
        fn(ax)
        fig.tight_layout(pad=2)
        save(fig, filename)
        plt.close(fig)

    print("\nComposing poster 1...")
    poster = build_poster()
    save(poster, "poster.png")
    plt.close(poster)

    print("\nGenerating poster 2 panels...")
    advanced = [
        ("07_pyramid.png",      plot_pyramid,   (9,    5.5)),
        ("08_bubbles.png",      plot_bubbles,   (8,    6  )),
        ("09_lollipop.png",     plot_lollipop,  (8,    7  )),
        ("11_life_grid.png",    plot_life_grid, (16,   7  )),
    ]
    for filename, fn, fsize in advanced:
        fig, ax = plt.subplots(figsize=fsize, facecolor=BG)
        fn(ax)
        fig.tight_layout(pad=2)
        save(fig, filename)
        plt.close(fig)

    # Radar needs polar axes
    fig, ax = plt.subplots(figsize=(7, 7), facecolor=BG,
                           subplot_kw=dict(polar=True))
    plot_radar(ax)
    fig.tight_layout(pad=2)
    save(fig, "10_radar.png")
    plt.close(fig)

    print("\nComposing poster 2...")
    poster2 = build_poster2()
    save(poster2, "poster2.png")
    plt.close(poster2)

    print("\nDone. All files are in scripts/graphs/")


if __name__ == "__main__":
    main()
